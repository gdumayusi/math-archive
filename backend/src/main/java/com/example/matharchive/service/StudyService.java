package com.example.matharchive.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.matharchive.domain.AppUser;
import com.example.matharchive.domain.PracticeRecord;
import com.example.matharchive.domain.PracticeStatus;
import com.example.matharchive.domain.Question;
import com.example.matharchive.dto.KnowledgeStatDto;
import com.example.matharchive.dto.PracticeRecordRequest;
import com.example.matharchive.dto.RecentPracticeDto;
import com.example.matharchive.dto.StudyStatsResponse;
import com.example.matharchive.dto.StudyTimelinePointDto;
import com.example.matharchive.dto.SubjectProgressDto;
import com.example.matharchive.repository.AppUserRepository;
import com.example.matharchive.repository.PracticeRecordRepository;
import com.example.matharchive.repository.QuestionRepository;

@Service
public class StudyService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final AppUserRepository appUserRepository;
    private final QuestionRepository questionRepository;
    private final PracticeRecordRepository practiceRecordRepository;
    private final JdbcTemplate jdbcTemplate;

    public StudyService(
            AppUserRepository appUserRepository,
            QuestionRepository questionRepository,
            PracticeRecordRepository practiceRecordRepository,
            JdbcTemplate jdbcTemplate
    ) {
        this.appUserRepository = appUserRepository;
        this.questionRepository = questionRepository;
        this.practiceRecordRepository = practiceRecordRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public void recordPractice(Long userId, PracticeRecordRequest request) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        Question question = questionRepository.findById(request.questionId())
                .orElseThrow(() -> new IllegalArgumentException("题目不存在"));

        PracticeRecord record = new PracticeRecord();
        record.setUser(user);
        record.setQuestion(question);
        record.setStatus(PracticeStatus.from(request.status()));
        record.setSourcePage(normalizeSourcePage(request.sourcePage()));
        record.setPracticedAt(LocalDateTime.now());
        practiceRecordRepository.save(record);
    }

    @Transactional(readOnly = true)
    public StudyStatsResponse getStudyStats(Long userId) {
        appUserRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        int totalPractices = safeToInt(practiceRecordRepository.countByUser_Id(userId));
        int masteredCount = safeToInt(practiceRecordRepository.countByUser_IdAndStatus(userId, PracticeStatus.MASTERED));
        int reviewCount = safeToInt(practiceRecordRepository.countByUser_IdAndStatus(userId, PracticeStatus.REVIEW));
        int mistakeCount = safeToInt(practiceRecordRepository.countByUser_IdAndStatus(userId, PracticeStatus.MISTAKE));
        int accuracyRate = calculateRate(masteredCount, masteredCount + reviewCount + mistakeCount);
        LatestStatusSummary latestStatusSummary = summarizeLatestStatuses(userId);

        Integer activeDays = safeQueryForObject(
                "SELECT COALESCE(COUNT(DISTINCT DATE(practiced_at)), 0) FROM practice_record WHERE user_id = ?",
                Integer.class,
                0,
                userId
        );

        return new StudyStatsResponse(
                totalPractices,
                masteredCount,
                reviewCount,
                mistakeCount,
                accuracyRate,
                activeDays == null ? 0 : activeDays,
                latestStatusSummary.currentMasteredCount(),
                latestStatusSummary.pendingReviewCount(),
                safeSubjectProgress(userId),
                safeTimeline(userId),
                safeWeakPoints(userId),
                safeRecentRecords(userId)
        );
    }

    private LatestStatusSummary summarizeLatestStatuses(Long userId) {
        Set<Long> resolvedQuestions = new HashSet<>();
        int currentMasteredCount = 0;
        int pendingReviewCount = 0;

        for (PracticeRecord record : practiceRecordRepository.findByUser_IdOrderByQuestion_IdAscPracticedAtDescIdDesc(userId)) {
            Long questionId = record.getQuestion().getId();
            if (resolvedQuestions.contains(questionId)) {
                continue;
            }
            if (record.getStatus() == PracticeStatus.VIEWED) {
                continue;
            }
            resolvedQuestions.add(questionId);
            if (record.getStatus() == PracticeStatus.MASTERED) {
                currentMasteredCount++;
            }
            if (record.getStatus() == PracticeStatus.REVIEW || record.getStatus() == PracticeStatus.MISTAKE) {
                pendingReviewCount++;
            }
        }

        return new LatestStatusSummary(currentMasteredCount, pendingReviewCount);
    }

    private List<SubjectProgressDto> safeSubjectProgress(Long userId) {
        try {
            return getSubjectProgress(userId);
        } catch (RuntimeException exception) {
            return List.of();
        }
    }

    private List<StudyTimelinePointDto> safeTimeline(Long userId) {
        try {
            return getTimeline(userId);
        } catch (RuntimeException exception) {
            return buildEmptyTimeline();
        }
    }

    private List<KnowledgeStatDto> safeWeakPoints(Long userId) {
        try {
            return getWeakPoints(userId);
        } catch (RuntimeException exception) {
            return List.of();
        }
    }

    private List<RecentPracticeDto> safeRecentRecords(Long userId) {
        try {
            return getRecentRecords(userId);
        } catch (RuntimeException exception) {
            return List.of();
        }
    }

    private List<SubjectProgressDto> getSubjectProgress(Long userId) {
        return jdbcTemplate.query("""
                SELECT q.subject,
                       COUNT(*) AS total_count,
                       SUM(CASE WHEN pr.status = 'MASTERED' THEN 1 ELSE 0 END) AS mastered_count,
                       SUM(CASE WHEN pr.status = 'MISTAKE' THEN 1 ELSE 0 END) AS mistake_count
                FROM practice_record pr
                INNER JOIN question q ON q.id = pr.question_id
                WHERE pr.user_id = ?
                GROUP BY q.subject
                ORDER BY total_count DESC, q.subject ASC
                """, (resultSet, rowNum) -> {
            int totalCount = resultSet.getInt("total_count");
            int masteredCount = resultSet.getInt("mastered_count");
            int mistakeCount = resultSet.getInt("mistake_count");
            return new SubjectProgressDto(
                    resultSet.getString("subject"),
                    totalCount,
                    masteredCount,
                    mistakeCount,
                    calculateRate(masteredCount, totalCount)
            );
        }, userId);
    }

    private List<StudyTimelinePointDto> getTimeline(Long userId) {
        Map<String, StudyTimelinePointDto> timeline = new LinkedHashMap<>();
        buildEmptyTimeline().forEach(item -> timeline.put(item.date(), item));

        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT DATE_FORMAT(practiced_at, '%Y-%m-%d') AS practice_date,
                       SUM(CASE WHEN status = 'VIEWED' THEN 1 ELSE 0 END) AS viewed_count,
                       SUM(CASE WHEN status = 'MASTERED' THEN 1 ELSE 0 END) AS mastered_count,
                       SUM(CASE WHEN status = 'REVIEW' THEN 1 ELSE 0 END) AS review_count,
                       SUM(CASE WHEN status = 'MISTAKE' THEN 1 ELSE 0 END) AS mistake_count
                FROM practice_record
                WHERE user_id = ?
                  AND practiced_at >= DATE_SUB(CURDATE(), INTERVAL 6 DAY)
                GROUP BY DATE(practiced_at)
                ORDER BY practice_date ASC
                """, userId);

        for (Map<String, Object> row : rows) {
            String date = String.valueOf(row.get("practice_date"));
            timeline.put(date, new StudyTimelinePointDto(
                    date,
                    getInt(row.get("viewed_count")),
                    getInt(row.get("mastered_count")),
                    getInt(row.get("review_count")),
                    getInt(row.get("mistake_count"))
            ));
        }

        return new ArrayList<>(timeline.values());
    }

    private List<StudyTimelinePointDto> buildEmptyTimeline() {
        List<StudyTimelinePointDto> timeline = new ArrayList<>();
        for (int offset = 6; offset >= 0; offset--) {
            LocalDate date = LocalDate.now().minusDays(offset);
            timeline.add(new StudyTimelinePointDto(date.toString(), 0, 0, 0, 0));
        }
        return timeline;
    }

    private List<KnowledgeStatDto> getWeakPoints(Long userId) {
        return jdbcTemplate.query("""
                SELECT q.chapter_name,
                       COALESCE(q.knowledge_tags, '') AS knowledge_tags,
                       COUNT(*) AS total_count,
                       SUM(CASE WHEN pr.status = 'MASTERED' THEN 1 ELSE 0 END) AS mastered_count,
                       SUM(CASE WHEN pr.status = 'MISTAKE' THEN 1 ELSE 0 END) AS mistake_count
                FROM practice_record pr
                INNER JOIN question q ON q.id = pr.question_id
                WHERE pr.user_id = ?
                GROUP BY q.chapter_name, q.knowledge_tags
                ORDER BY
                    CASE
                        WHEN COUNT(*) = 0 THEN 0
                        ELSE SUM(CASE WHEN pr.status = 'MASTERED' THEN 1 ELSE 0 END) / COUNT(*)
                    END ASC,
                    mistake_count DESC,
                    total_count DESC,
                    q.chapter_name ASC
                LIMIT 6
                """, (resultSet, rowNum) -> toKnowledgeStat(resultSet), userId);
    }

    private KnowledgeStatDto toKnowledgeStat(ResultSet resultSet) throws SQLException {
        int totalCount = resultSet.getInt("total_count");
        int masteredCount = resultSet.getInt("mastered_count");
        int mistakeCount = resultSet.getInt("mistake_count");
        return new KnowledgeStatDto(
                resultSet.getString("chapter_name"),
                resultSet.getString("knowledge_tags"),
                totalCount,
                masteredCount,
                mistakeCount,
                calculateRate(masteredCount, totalCount)
        );
    }

    private List<RecentPracticeDto> getRecentRecords(Long userId) {
        return jdbcTemplate.query("""
                SELECT q.id,
                       q.archive_code,
                       q.subject,
                       q.chapter_name,
                       pr.status,
                       pr.practiced_at
                FROM practice_record pr
                INNER JOIN question q ON q.id = pr.question_id
                WHERE pr.user_id = ?
                ORDER BY pr.practiced_at DESC
                LIMIT 8
                """, (resultSet, rowNum) -> new RecentPracticeDto(
                resultSet.getLong("id"),
                resultSet.getString("archive_code"),
                resultSet.getString("subject"),
                resultSet.getString("chapter_name"),
                resultSet.getString("status"),
                resultSet.getTimestamp("practiced_at").toLocalDateTime().format(DATE_TIME_FORMATTER)
        ), userId);
    }

    private String normalizeSourcePage(String sourcePage) {
        if (sourcePage == null || sourcePage.isBlank()) {
            return "LIBRARY";
        }
        String normalized = sourcePage.trim().toUpperCase();
        return normalized.length() > 32 ? normalized.substring(0, 32) : normalized;
    }

    private int calculateRate(int numerator, int denominator) {
        if (denominator <= 0) {
            return 0;
        }
        return BigDecimal.valueOf(numerator * 100.0 / denominator)
                .setScale(0, RoundingMode.HALF_UP)
                .intValue();
    }

    private int safeToInt(long value) {
        return Math.toIntExact(Math.min(value, Integer.MAX_VALUE));
    }

    private int getInt(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        return 0;
    }

    private <T> T safeQueryForObject(String sql, Class<T> requiredType, T fallback, Object... args) {
        try {
            T result = jdbcTemplate.queryForObject(sql, requiredType, args);
            return result == null ? fallback : result;
        } catch (RuntimeException exception) {
            return fallback;
        }
    }

    private record LatestStatusSummary(int currentMasteredCount, int pendingReviewCount) {
    }
}
