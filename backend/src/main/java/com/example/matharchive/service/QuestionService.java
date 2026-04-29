package com.example.matharchive.service;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.example.matharchive.domain.PracticeRecord;
import com.example.matharchive.domain.PracticeStatus;
import com.example.matharchive.domain.Question;
import com.example.matharchive.dto.PageResponse;
import com.example.matharchive.dto.QuestionDetailDto;
import com.example.matharchive.dto.QuestionItemDto;
import com.example.matharchive.repository.AppUserRepository;
import com.example.matharchive.repository.FavoriteRepository;
import com.example.matharchive.repository.MistakeRecordRepository;
import com.example.matharchive.repository.PracticeRecordRepository;
import com.example.matharchive.repository.QuestionRepository;

import jakarta.persistence.criteria.Predicate;

@Service
public class QuestionService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final QuestionRepository questionRepository;
    private final AppUserRepository appUserRepository;
    private final FavoriteRepository favoriteRepository;
    private final MistakeRecordRepository mistakeRecordRepository;
    private final PracticeRecordRepository practiceRecordRepository;
    private final JdbcTemplate jdbcTemplate;

    public QuestionService(
            QuestionRepository questionRepository,
            AppUserRepository appUserRepository,
            FavoriteRepository favoriteRepository,
            MistakeRecordRepository mistakeRecordRepository,
            PracticeRecordRepository practiceRecordRepository,
            JdbcTemplate jdbcTemplate
    ) {
        this.questionRepository = questionRepository;
        this.appUserRepository = appUserRepository;
        this.favoriteRepository = favoriteRepository;
        this.mistakeRecordRepository = mistakeRecordRepository;
        this.practiceRecordRepository = practiceRecordRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    public PageResponse<QuestionItemDto> searchQuestions(
            Long userId,
            String keyword,
            String subjects,
            String chapterNames,
            String knowledgeTags,
            String paperNames,
            String years,
            String questionTypes,
            String difficulties,
            int page,
            int size,
            String sort
    ) {
        Pageable pageable = PageRequest.of(page, size, resolveSort(sort));
        Specification<Question> specification = buildSpecification(
                keyword,
                subjects,
                chapterNames,
                knowledgeTags,
                paperNames,
                years,
                questionTypes,
                difficulties
        );
        Page<Question> questionPage = questionRepository.findAll(specification, pageable);
        Map<Long, UserPracticeState> practiceStateMap = loadPracticeStateMap(
                userId,
                questionPage.getContent().stream().map(Question::getId).toList()
        );
        Page<QuestionItemDto> result = questionPage.map(question -> toItemDto(question, userId, practiceStateMap.get(question.getId())));
        return PageResponse.from(result);
    }

    public QuestionDetailDto getQuestionDetail(Long id, Long userId) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("题目不存在"));
        UserPracticeState practiceState = loadPracticeState(userId, id);
        return toDetailDto(question, userId, practiceState);
    }

    @Transactional(readOnly = true)
    public List<QuestionItemDto> getFavorites(Long userId) {
        appUserRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        List<QuestionItemDto> items = jdbcTemplate.query("""
                SELECT id, archive_code, year_value, paper_name, subject, chapter_name,
                       knowledge_tags, question_type, difficulty_level, stem_text,
                       option_a, option_b, option_c, option_d, created_date, favorited, mistake
                FROM v_user_favorites
                WHERE user_id = ?
                ORDER BY collected_at DESC
                """, (resultSet, rowNum) -> new QuestionItemDto(
                resultSet.getLong("id"),
                resultSet.getString("archive_code"),
                resultSet.getInt("year_value"),
                resultSet.getString("paper_name"),
                resultSet.getString("subject"),
                resultSet.getString("chapter_name"),
                resultSet.getString("knowledge_tags"),
                resultSet.getString("question_type"),
                resultSet.getString("difficulty_level"),
                resultSet.getString("stem_text"),
                resultSet.getString("option_a"),
                resultSet.getString("option_b"),
                resultSet.getString("option_c"),
                resultSet.getString("option_d"),
                resultSet.getString("created_date"),
                resultSet.getBoolean("favorited"),
                resultSet.getBoolean("mistake"),
                null,
                null
        ), userId);
        return enrichPracticeStates(items, userId);
    }

    @Transactional(readOnly = true)
    public List<QuestionItemDto> getMistakes(Long userId) {
        appUserRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        List<QuestionItemDto> items = jdbcTemplate.query("""
                SELECT id, archive_code, year_value, paper_name, subject, chapter_name,
                       knowledge_tags, question_type, difficulty_level, stem_text,
                       option_a, option_b, option_c, option_d, created_date, favorited, mistake
                FROM v_user_mistakes
                WHERE user_id = ?
                ORDER BY marked_at DESC
                """, (resultSet, rowNum) -> new QuestionItemDto(
                resultSet.getLong("id"),
                resultSet.getString("archive_code"),
                resultSet.getInt("year_value"),
                resultSet.getString("paper_name"),
                resultSet.getString("subject"),
                resultSet.getString("chapter_name"),
                resultSet.getString("knowledge_tags"),
                resultSet.getString("question_type"),
                resultSet.getString("difficulty_level"),
                resultSet.getString("stem_text"),
                resultSet.getString("option_a"),
                resultSet.getString("option_b"),
                resultSet.getString("option_c"),
                resultSet.getString("option_d"),
                resultSet.getString("created_date"),
                resultSet.getBoolean("favorited"),
                resultSet.getBoolean("mistake"),
                null,
                null
        ), userId);
        return enrichPracticeStates(items, userId);
    }

    @Transactional
    public void addFavorite(Long userId, Long questionId) {
        appUserRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        questionRepository.findById(questionId).orElseThrow(() -> new IllegalArgumentException("题目不存在"));
        callProcedure("CALL sp_add_favorite(?, ?)", userId, questionId);
    }

    @Transactional
    public void removeFavorite(Long userId, Long questionId) {
        callProcedure("CALL sp_remove_favorite(?, ?)", userId, questionId);
    }

    @Transactional
    public void addMistake(Long userId, Long questionId, String note) {
        appUserRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        questionRepository.findById(questionId).orElseThrow(() -> new IllegalArgumentException("题目不存在"));
        callProcedure("CALL sp_add_mistake(?, ?, ?)", userId, questionId, note);
    }

    @Transactional
    public void removeMistake(Long userId, Long questionId) {
        callProcedure("CALL sp_remove_mistake(?, ?)", userId, questionId);
    }

    private Specification<Question> buildSpecification(
            String keyword,
            String subjects,
            String chapterNames,
            String knowledgeTags,
            String paperNames,
            String years,
            String questionTypes,
            String difficulties
    ) {
        Set<String> subjectSet = parseStringSet(subjects);
        Set<String> chapterNameSet = parseStringSet(chapterNames);
        Set<String> knowledgeTagSet = parseStringSet(knowledgeTags);
        Set<String> paperNameSet = parseStringSet(paperNames);
        Set<Integer> yearSet = parseIntegerSet(years);
        Set<String> typeSet = parseStringSet(questionTypes);
        Set<String> difficultySet = parseStringSet(difficulties);

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new java.util.ArrayList<>();

            if (StringUtils.hasText(keyword)) {
                String likeKeyword = "%" + keyword.trim() + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(root.get("archiveCode"), likeKeyword),
                        criteriaBuilder.like(root.get("stemText"), likeKeyword),
                        criteriaBuilder.like(root.get("chapterName"), likeKeyword),
                        criteriaBuilder.like(root.get("knowledgeTags"), likeKeyword),
                        criteriaBuilder.like(root.get("paperName"), likeKeyword)
                ));
            }
            if (!subjectSet.isEmpty()) {
                predicates.add(root.get("subject").in(subjectSet));
            }
            if (!chapterNameSet.isEmpty()) {
                predicates.add(root.get("chapterName").in(chapterNameSet));
            }
            if (!knowledgeTagSet.isEmpty()) {
                predicates.add(criteriaBuilder.or(knowledgeTagSet.stream()
                        .map(tag -> criteriaBuilder.like(root.get("knowledgeTags"), "%" + tag + "%"))
                        .toArray(Predicate[]::new)));
            }
            if (!paperNameSet.isEmpty()) {
                predicates.add(root.get("paperName").in(paperNameSet));
            }
            if (!yearSet.isEmpty()) {
                predicates.add(root.get("yearValue").in(yearSet));
            }
            if (!typeSet.isEmpty()) {
                predicates.add(root.get("questionType").in(typeSet));
            }
            if (!difficultySet.isEmpty()) {
                predicates.add(root.get("difficultyLevel").in(difficultySet));
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Sort resolveSort(String sort) {
        return switch (sort == null ? "" : sort) {
            case "difficulty" -> Sort.by(Sort.Order.asc("difficultyLevel"), Sort.Order.desc("yearValue"));
            case "archiveCode" -> Sort.by(Sort.Order.asc("archiveCode"));
            default -> Sort.by(Sort.Order.desc("yearValue"), Sort.Order.asc("archiveCode"));
        };
    }

    private QuestionItemDto toItemDto(Question question, Long userId, UserPracticeState practiceState) {
        boolean favorited = userId != null && favoriteRepository.existsByUser_IdAndQuestion_Id(userId, question.getId());
        boolean mistake = userId != null && mistakeRecordRepository.existsByUser_IdAndQuestion_Id(userId, question.getId());
        return new QuestionItemDto(
                question.getId(),
                question.getArchiveCode(),
                question.getYearValue(),
                question.getPaperName(),
                question.getSubject(),
                question.getChapterName(),
                question.getKnowledgeTags(),
                question.getQuestionType(),
                question.getDifficultyLevel(),
                question.getStemText(),
                question.getOptionA(),
                question.getOptionB(),
                question.getOptionC(),
                question.getOptionD(),
                question.getCreatedDate().format(DATE_FORMATTER),
                favorited,
                mistake,
                practiceState == null ? null : practiceState.status(),
                practiceState == null ? null : practiceState.practicedAt()
        );
    }

    private QuestionDetailDto toDetailDto(Question question, Long userId, UserPracticeState practiceState) {
        QuestionItemDto item = toItemDto(question, userId, practiceState);
        return new QuestionDetailDto(
                item.id(),
                item.archiveCode(),
                item.yearValue(),
                item.paperName(),
                item.subject(),
                item.chapterName(),
                item.knowledgeTags(),
                item.questionType(),
                item.difficultyLevel(),
                item.stemText(),
                item.optionA(),
                item.optionB(),
                item.optionC(),
                item.optionD(),
                question.getAnswerText(),
                question.getAnalysisText(),
                item.createdDate(),
                item.favorited(),
                item.mistake(),
                item.latestPracticeStatus(),
                item.latestPracticedAt()
        );
    }

    private List<QuestionItemDto> enrichPracticeStates(List<QuestionItemDto> items, Long userId) {
        Map<Long, UserPracticeState> practiceStateMap = loadPracticeStateMap(
                userId,
                items.stream().map(QuestionItemDto::id).toList()
        );
        return items.stream()
                .map(item -> {
                    UserPracticeState practiceState = practiceStateMap.get(item.id());
                    return new QuestionItemDto(
                            item.id(),
                            item.archiveCode(),
                            item.yearValue(),
                            item.paperName(),
                            item.subject(),
                            item.chapterName(),
                            item.knowledgeTags(),
                            item.questionType(),
                            item.difficultyLevel(),
                            item.stemText(),
                            item.optionA(),
                            item.optionB(),
                            item.optionC(),
                            item.optionD(),
                            item.createdDate(),
                            item.favorited(),
                            item.mistake(),
                            practiceState == null ? null : practiceState.status(),
                            practiceState == null ? null : practiceState.practicedAt()
                    );
                })
                .toList();
    }

    private UserPracticeState loadPracticeState(Long userId, Long questionId) {
        if (userId == null || questionId == null) {
            return null;
        }
        return selectLatestPracticeState(practiceRecordRepository.findLatestByUserIdAndQuestionId(userId, questionId));
    }

    private Map<Long, UserPracticeState> loadPracticeStateMap(Long userId, List<Long> questionIds) {
        if (userId == null || questionIds == null || questionIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, UserPracticeState> result = new LinkedHashMap<>();
        Map<Long, UserPracticeState> viewedFallback = new LinkedHashMap<>();
        for (PracticeRecord record : practiceRecordRepository.findByUser_IdAndQuestion_IdInOrderByQuestion_IdAscPracticedAtDescIdDesc(userId, questionIds)) {
            Long questionId = record.getQuestion().getId();
            UserPracticeState practiceState = toUserPracticeState(record);
            if (record.getStatus() == PracticeStatus.VIEWED) {
                viewedFallback.putIfAbsent(questionId, practiceState);
                continue;
            }
            result.putIfAbsent(questionId, practiceState);
        }
        for (Long questionId : questionIds) {
            if (!result.containsKey(questionId) && viewedFallback.containsKey(questionId)) {
                result.put(questionId, viewedFallback.get(questionId));
            }
        }
        return result;
    }

    private UserPracticeState selectLatestPracticeState(List<PracticeRecord> records) {
        UserPracticeState viewedFallback = null;
        for (PracticeRecord record : records) {
            UserPracticeState practiceState = toUserPracticeState(record);
            if (record.getStatus() == PracticeStatus.VIEWED) {
                if (viewedFallback == null) {
                    viewedFallback = practiceState;
                }
                continue;
            }
            return practiceState;
        }
        return viewedFallback;
    }

    private UserPracticeState toUserPracticeState(PracticeRecord record) {
        return new UserPracticeState(
                record.getStatus().name(),
                record.getPracticedAt().format(DATE_TIME_FORMATTER)
        );
    }

    private Set<String> parseStringSet(String raw) {
        if (!StringUtils.hasText(raw)) {
            return Collections.emptySet();
        }
        return Stream.of(raw.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
    }

    private Set<Integer> parseIntegerSet(String raw) {
        if (!StringUtils.hasText(raw)) {
            return Collections.emptySet();
        }
        return Stream.of(raw.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .map(Integer::parseInt)
                .collect(Collectors.toSet());
    }

    private void callProcedure(String sql, Object... args) {
        try {
            jdbcTemplate.update(sql, args);
        } catch (DataAccessException exception) {
            String message = exception.getMostSpecificCause() == null
                    ? "数据库操作失败"
                    : exception.getMostSpecificCause().getMessage();
            throw new IllegalArgumentException(message);
        }
    }

    private record UserPracticeState(String status, String practicedAt) {
    }
}
