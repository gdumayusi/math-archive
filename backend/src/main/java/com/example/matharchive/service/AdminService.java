package com.example.matharchive.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.example.matharchive.domain.AppUser;
import com.example.matharchive.domain.OperationLog;
import com.example.matharchive.domain.Question;
import com.example.matharchive.domain.ReviewStatus;
import com.example.matharchive.domain.SystemSetting;
import com.example.matharchive.domain.UserRole;
import com.example.matharchive.dto.AdminChartPointDto;
import com.example.matharchive.dto.AdminDataStatsDto;
import com.example.matharchive.dto.AdminOverviewDto;
import com.example.matharchive.dto.AdminQuestionReviewDto;
import com.example.matharchive.dto.AdminReviewActionRequest;
import com.example.matharchive.dto.AdminSystemSettingsDto;
import com.example.matharchive.dto.AdminSystemSettingsUpdateRequest;
import com.example.matharchive.dto.OperationLogDto;
import com.example.matharchive.dto.PageResponse;
import com.example.matharchive.dto.QuestionItemDto;
import com.example.matharchive.dto.QuestionUpsertRequest;
import com.example.matharchive.dto.UserRowDto;
import com.example.matharchive.dto.UserUpsertRequest;
import com.example.matharchive.repository.AppUserRepository;
import com.example.matharchive.repository.FavoriteRepository;
import com.example.matharchive.repository.MistakeRecordRepository;
import com.example.matharchive.repository.OperationLogRepository;
import com.example.matharchive.repository.PracticeRecordRepository;
import com.example.matharchive.repository.QuestionRepository;
import com.example.matharchive.repository.SystemSettingRepository;

@Service
public class AdminService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private final QuestionRepository questionRepository;
    private final AppUserRepository appUserRepository;
    private final FavoriteRepository favoriteRepository;
    private final MistakeRecordRepository mistakeRecordRepository;
    private final PracticeRecordRepository practiceRecordRepository;
    private final OperationLogRepository operationLogRepository;
    private final SystemSettingRepository systemSettingRepository;

    public AdminService(
            QuestionRepository questionRepository,
            AppUserRepository appUserRepository,
            FavoriteRepository favoriteRepository,
            MistakeRecordRepository mistakeRecordRepository,
            PracticeRecordRepository practiceRecordRepository,
            OperationLogRepository operationLogRepository,
            SystemSettingRepository systemSettingRepository
    ) {
        this.questionRepository = questionRepository;
        this.appUserRepository = appUserRepository;
        this.favoriteRepository = favoriteRepository;
        this.mistakeRecordRepository = mistakeRecordRepository;
        this.practiceRecordRepository = practiceRecordRepository;
        this.operationLogRepository = operationLogRepository;
        this.systemSettingRepository = systemSettingRepository;
    }

    public AdminOverviewDto getOverview() {
        List<AppUser> users = appUserRepository.findAll();
        List<Question> questions = questionRepository.findAll();

        long totalUsers = users.size();
        long activeUsers = users.stream().filter(AppUser::isActive).count();
        long todayNewUsers = users.stream()
                .filter(user -> user.getCreatedAt() != null && user.getCreatedAt().toLocalDate().equals(LocalDate.now()))
                .count();
        long pendingQuestions = questions.stream().filter(question -> reviewStatusOf(question) == ReviewStatus.PENDING).count();
        long approvedQuestions = questions.stream().filter(question -> reviewStatusOf(question) == ReviewStatus.APPROVED).count();
        long rejectedQuestions = questions.stream().filter(question -> reviewStatusOf(question) == ReviewStatus.REJECTED).count();

        return new AdminOverviewDto(
                totalUsers,
                activeUsers,
                totalUsers - activeUsers,
                todayNewUsers,
                questions.size(),
                pendingQuestions,
                approvedQuestions,
                rejectedQuestions,
                mistakeRecordRepository.count(),
                favoriteRepository.count()
        );
    }

    public PageResponse<QuestionItemDto> getQuestions(String keyword, int page, int size) {
        Page<Question> result;
        if (StringUtils.hasText(keyword)) {
            String likeKeyword = "%" + keyword.trim() + "%";
            result = questionRepository.findAll((root, query, criteriaBuilder) -> criteriaBuilder.or(
                            criteriaBuilder.like(root.get("archiveCode"), likeKeyword),
                            criteriaBuilder.like(root.get("paperName"), likeKeyword),
                            criteriaBuilder.like(root.get("chapterName"), likeKeyword),
                            criteriaBuilder.like(root.get("knowledgeTags"), likeKeyword),
                            criteriaBuilder.like(root.get("stemText"), likeKeyword)
                    ),
                    PageRequest.of(page, size, Sort.by(Sort.Order.desc("yearValue"), Sort.Order.asc("archiveCode"))));
        } else {
            result = questionRepository.findAll(
                    PageRequest.of(page, size, Sort.by(Sort.Order.desc("yearValue"), Sort.Order.asc("archiveCode")))
            );
        }
        return PageResponse.from(result.map(this::toAdminRow));
    }

    public PageResponse<AdminQuestionReviewDto> getQuestionReviews(String keyword, String status, int page, int size) {
        List<Question> filtered = questionRepository.findAll().stream()
                .filter(question -> matchQuestionKeyword(question, keyword))
                .filter(question -> matchStatus(question, status))
                .sorted(Comparator.comparing(Question::getSubmittedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();

        int from = Math.min(page * size, filtered.size());
        int to = Math.min(from + size, filtered.size());
        Page<AdminQuestionReviewDto> result = new PageImpl<>(
                filtered.subList(from, to).stream().map(this::toReviewRow).toList(),
                PageRequest.of(page, size),
                filtered.size()
        );
        return PageResponse.from(result);
    }

    @Transactional
    public AdminQuestionReviewDto reviewQuestion(Long id, AdminReviewActionRequest request) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("题目不存在"));

        String action = request.action().trim().toUpperCase(Locale.ROOT);
        if ("APPROVE".equals(action)) {
            question.setReviewStatus(ReviewStatus.APPROVED);
            question.setRejectReason(null);
            log("审核通过", "题目审核", "通过题目 " + question.getArchiveCode(), "成功");
        } else if ("REJECT".equals(action)) {
            question.setReviewStatus(ReviewStatus.REJECTED);
            question.setRejectReason(trimToNull(request.rejectReason()));
            log("审核驳回", "题目审核", "驳回题目 " + question.getArchiveCode(), "成功");
        } else {
            throw new IllegalArgumentException("不支持的审核操作");
        }

        question.setUpdatedAt(LocalDateTime.now());
        return toReviewRow(questionRepository.save(question));
    }

    @Transactional
    public QuestionItemDto createQuestion(QuestionUpsertRequest request) {
        if (questionRepository.existsByArchiveCode(request.archiveCode().trim())) {
            throw new IllegalArgumentException("题目编号已存在");
        }

        Question question = new Question();
        applyQuestionRequest(question, request);
        question.setCreatedDate(LocalDate.now());
        question.setSubmittedAt(LocalDateTime.now());
        question.setUpdatedAt(LocalDateTime.now());
        question.setReviewStatus(ReviewStatus.PENDING);
        if (!StringUtils.hasText(question.getSubmitterName())) {
            question.setSubmitterName("系统管理员");
        }

        Question saved = questionRepository.save(question);
        log("新增题目", "题目审核", "新增题目 " + saved.getArchiveCode(), "成功");
        return toAdminRow(saved);
    }

    @Transactional
    public QuestionItemDto updateQuestion(Long id, QuestionUpsertRequest request) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("题目不存在"));

        String archiveCode = request.archiveCode().trim();
        if (!question.getArchiveCode().equals(archiveCode) && questionRepository.existsByArchiveCode(archiveCode)) {
            throw new IllegalArgumentException("题目编号已存在");
        }

        applyQuestionRequest(question, request);
        question.setUpdatedAt(LocalDateTime.now());
        Question saved = questionRepository.save(question);
        log("编辑题目", "题目审核", "编辑题目 " + saved.getArchiveCode(), "成功");
        return toAdminRow(saved);
    }

    @Transactional
    public void deleteQuestion(Long id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("题目不存在"));

        favoriteRepository.deleteByQuestion_Id(id);
        mistakeRecordRepository.deleteByQuestion_Id(id);
        practiceRecordRepository.deleteByQuestion_Id(id);
        questionRepository.delete(question);
        log("删除题目", "题目审核", "删除题目 " + question.getArchiveCode(), "成功");
    }

    public List<UserRowDto> getUsers(String keyword, String role, Boolean active) {
        String normalizedKeyword = normalizeKeyword(keyword);
        return appUserRepository.findAll(Sort.by(Sort.Order.desc("createdAt"))).stream()
                .filter(user -> matchUserKeyword(user, normalizedKeyword))
                .filter(user -> !StringUtils.hasText(role) || user.getRole().name().equalsIgnoreCase(role))
                .filter(user -> active == null || user.isActive() == active)
                .map(this::toUserRow)
                .toList();
    }

    @Transactional
    public UserRowDto createUser(UserUpsertRequest request) {
        String username = request.username().trim();
        if (appUserRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("用户名已存在");
        }

        AppUser user = new AppUser();
        user.setUsername(username);
        applyUserRequest(user, request, true);
        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        user.setAvatarKey("classic");
        user.setBio("这个用户还没有填写个人简介。");
        if (!StringUtils.hasText(user.getStudentNo())) {
            user.setStudentNo("2023" + String.format("%06d", appUserRepository.count() + 1));
        }

        UserRowDto row = toUserRow(appUserRepository.save(user));
        log("新增用户", "用户管理", "新增用户 " + user.getUsername(), "成功");
        return row;
    }

    @Transactional
    public UserRowDto updateUser(Long id, UserUpsertRequest request) {
        AppUser user = appUserRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        String username = request.username().trim();
        if (!user.getUsername().equals(username) && appUserRepository.existsByUsernameAndIdNot(username, id)) {
            throw new IllegalArgumentException("用户名已存在");
        }
        if ("admin".equalsIgnoreCase(user.getUsername()) && !"ADMIN".equalsIgnoreCase(request.role().trim())) {
            throw new IllegalArgumentException("默认管理员账号不能降级");
        }

        user.setUsername(username);
        applyUserRequest(user, request, false);
        user.setUpdatedAt(LocalDateTime.now());
        UserRowDto row = toUserRow(appUserRepository.save(user));
        log("编辑用户", "用户管理", "编辑用户 " + user.getUsername(), "成功");
        return row;
    }

    @Transactional
    public void deleteUser(Long id) {
        AppUser user = appUserRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        if ("admin".equalsIgnoreCase(user.getUsername())) {
            throw new IllegalArgumentException("默认管理员账号不能删除");
        }

        favoriteRepository.deleteByUser_Id(id);
        mistakeRecordRepository.deleteByUser_Id(id);
        practiceRecordRepository.deleteByUser_Id(id);
        appUserRepository.delete(user);
        log("删除用户", "用户管理", "删除用户 " + user.getUsername(), "成功");
    }

    public AdminDataStatsDto getDataStats() {
        return new AdminDataStatsDto(
                questionRepository.count(),
                appUserRepository.count(),
                mistakeRecordRepository.count(),
                favoriteRepository.count(),
                buildUserTrend(),
                buildMistakeTrend(),
                buildQuestionTypeDistribution(),
                buildKnowledgeTop(false),
                buildKnowledgeTop(true)
        );
    }

    public AdminSystemSettingsDto getSystemSettings() {
        Map<String, String> settings = systemSettingRepository.findAll().stream()
                .collect(Collectors.toMap(SystemSetting::getSettingKey, SystemSetting::getSettingValue));

        int totalStorageGb = 100;
        int usedStorageGb = Math.min(96, 12 + Math.toIntExact(questionRepository.count() / 10 + appUserRepository.count() / 5));

        return new AdminSystemSettingsDto(
                settings.getOrDefault("site_name", "考研数学真题分类管理系统"),
                settings.getOrDefault("admin_email", "admin@example.com"),
                settings.getOrDefault("contact_phone", "13800001234"),
                settings.getOrDefault("site_intro", "用于管理考研数学真题、题目审核、学习数据分析与用户服务。"),
                settings.getOrDefault("record_number", "陕ICP备12345678号-1"),
                settings.getOrDefault("version", "v1.0.0"),
                settings.getOrDefault("publish_date", "2026-04-01"),
                settings.getOrDefault("team_name", "考研数学项目组"),
                totalStorageGb,
                usedStorageGb,
                LocalDateTime.now().format(DATE_TIME_FORMATTER)
        );
    }

    @Transactional
    public AdminSystemSettingsDto updateSystemSettings(AdminSystemSettingsUpdateRequest request) {
        saveSetting("site_name", request.siteName());
        saveSetting("admin_email", defaultIfBlank(request.adminEmail(), "admin@example.com"));
        saveSetting("contact_phone", defaultIfBlank(request.contactPhone(), "13800001234"));
        saveSetting("site_intro", defaultIfBlank(request.siteIntro(), ""));
        saveSetting("record_number", defaultIfBlank(request.recordNumber(), ""));
        log("更新设置", "系统设置", "更新站点基础配置", "成功");
        return getSystemSettings();
    }

    public List<OperationLogDto> getOperationLogs(String keyword, String module) {
        String normalizedKeyword = normalizeKeyword(keyword);
        return operationLogRepository.findAll(Sort.by(Sort.Order.desc("operatedAt"))).stream()
                .filter(log -> !StringUtils.hasText(module) || log.getActionModule().equalsIgnoreCase(module))
                .filter(log -> !StringUtils.hasText(normalizedKeyword)
                        || contains(log.getOperatorName(), normalizedKeyword)
                        || contains(log.getActionDetail(), normalizedKeyword)
                        || contains(log.getActionType(), normalizedKeyword))
                .map(log -> new OperationLogDto(
                        log.getId(),
                        log.getOperatorName(),
                        log.getOperatorRole(),
                        log.getActionType(),
                        log.getActionModule(),
                        log.getActionDetail(),
                        log.getActionStatus(),
                        format(log.getOperatedAt()),
                        defaultIfBlank(log.getIpAddress(), "--")
                ))
                .toList();
    }

    private void applyQuestionRequest(Question question, QuestionUpsertRequest request) {
        question.setArchiveCode(request.archiveCode().trim());
        question.setYearValue(request.yearValue());
        question.setPaperName(request.paperName().trim());
        question.setSourceOrg(trimToNull(request.sourceOrg()));
        question.setSubject(request.subject().trim());
        question.setChapterName(request.chapterName().trim());
        question.setKnowledgeTags(request.knowledgeTags().trim());
        question.setQuestionType(request.questionType().trim());
        question.setDifficultyLevel(request.difficultyLevel().trim());
        question.setStemText(request.stemText().trim());
        question.setOptionA(trimToNull(request.optionA()));
        question.setOptionB(trimToNull(request.optionB()));
        question.setOptionC(trimToNull(request.optionC()));
        question.setOptionD(trimToNull(request.optionD()));
        question.setAnswerText(trimToNull(request.answerText()));
        question.setAnalysisText(trimToNull(request.analysisText()));
        if (!StringUtils.hasText(question.getSubmitterName())) {
            question.setSubmitterName("系统管理员");
        }
        if (question.getReviewStatus() == null) {
            question.setReviewStatus(ReviewStatus.PENDING);
        }
        if (question.getSubmittedAt() == null) {
            question.setSubmittedAt(LocalDateTime.now());
        }
    }

    private void applyUserRequest(AppUser user, UserUpsertRequest request, boolean creating) {
        String email = trimToNull(request.email());
        if (StringUtils.hasText(email) && !EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("邮箱格式不正确");
        }

        String password = trimToNull(request.password());
        if (creating && !StringUtils.hasText(password)) {
            throw new IllegalArgumentException("创建用户时必须填写密码");
        }

        user.setDisplayName(request.displayName().trim());
        user.setEmail(email);
        user.setPhone(trimToNull(request.phone()));
        user.setRole(parseRole(request.role()));
        user.setActive(Boolean.TRUE.equals(request.active()));
        if (StringUtils.hasText(password)) {
            user.setPassword(password);
        }
    }

    private UserRowDto toUserRow(AppUser user) {
        return new UserRowDto(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getRole().name(),
                user.getEmail(),
                user.getPhone(),
                user.isActive(),
                format(user.getCreatedAt()),
                format(user.getLastLoginAt()),
                Math.toIntExact(Math.min(practiceRecordRepository.countByUser_Id(user.getId()), Integer.MAX_VALUE))
        );
    }

    private QuestionItemDto toAdminRow(Question question) {
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
                question.getCreatedDate() == null ? "--" : question.getCreatedDate().toString(),
                false,
                false,
                null,
                null
        );
    }

    private AdminQuestionReviewDto toReviewRow(Question question) {
        return new AdminQuestionReviewDto(
                question.getId(),
                question.getArchiveCode(),
                question.getStemText(),
                question.getChapterName(),
                question.getKnowledgeTags(),
                defaultIfBlank(question.getSubmitterName(), "未填写"),
                question.getQuestionType(),
                question.getDifficultyLevel(),
                reviewStatusOf(question).name(),
                format(question.getSubmittedAt()),
                question.getAnswerText(),
                question.getAnalysisText(),
                question.getSourceOrg(),
                question.getRejectReason()
        );
    }

    private List<AdminChartPointDto> buildUserTrend() {
        List<AppUser> users = appUserRepository.findAll();
        Map<String, Integer> trend = new TreeMap<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            int count = (int) users.stream()
                    .filter(user -> user.getCreatedAt() != null && user.getCreatedAt().toLocalDate().equals(date))
                    .count();
            trend.put(date.toString(), count);
        }
        return trend.entrySet().stream().map(entry -> new AdminChartPointDto(entry.getKey(), entry.getValue())).toList();
    }

    private List<AdminChartPointDto> buildMistakeTrend() {
        Map<String, Integer> trend = new TreeMap<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            int count = (int) mistakeRecordRepository.findAll().stream()
                    .filter(item -> item.getCreatedAt() != null && item.getCreatedAt().toLocalDate().equals(date))
                    .count();
            trend.put(date.toString(), count);
        }
        return trend.entrySet().stream().map(entry -> new AdminChartPointDto(entry.getKey(), entry.getValue())).toList();
    }

    private List<AdminChartPointDto> buildQuestionTypeDistribution() {
        Map<String, Integer> distribution = new TreeMap<>();
        for (Question question : questionRepository.findAll()) {
            distribution.merge(defaultIfBlank(question.getQuestionType(), "未分类"), 1, Integer::sum);
        }
        return distribution.entrySet().stream()
                .map(entry -> new AdminChartPointDto(entry.getKey(), entry.getValue()))
                .toList();
    }

    private List<AdminChartPointDto> buildKnowledgeTop(boolean mistakesOnly) {
        Map<String, Integer> counts = new TreeMap<>();
        if (mistakesOnly) {
            mistakeRecordRepository.findAll().forEach(item ->
                    counts.merge(defaultIfBlank(item.getQuestion().getKnowledgeTags(), "未分类"), 1, Integer::sum)
            );
        } else {
            questionRepository.findAll().forEach(item ->
                    counts.merge(defaultIfBlank(item.getKnowledgeTags(), "未分类"), 1, Integer::sum)
            );
        }
        return counts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(10)
                .map(entry -> new AdminChartPointDto(entry.getKey(), entry.getValue()))
                .toList();
    }

    private void saveSetting(String key, String value) {
        SystemSetting setting = systemSettingRepository.findById(key).orElseGet(SystemSetting::new);
        setting.setSettingKey(key);
        setting.setSettingValue(value == null ? "" : value);
        setting.setUpdatedAt(LocalDateTime.now());
        systemSettingRepository.save(setting);
    }

    private void log(String type, String module, String detail, String status) {
        OperationLog log = new OperationLog();
        log.setOperatorName("admin");
        log.setOperatorRole("管理员");
        log.setActionType(type);
        log.setActionModule(module);
        log.setActionDetail(detail);
        log.setActionStatus(status);
        log.setOperatedAt(LocalDateTime.now());
        log.setIpAddress("127.0.0.1");
        operationLogRepository.save(log);
    }

    private boolean matchQuestionKeyword(Question question, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return true;
        }
        String normalized = normalizeKeyword(keyword);
        return contains(question.getArchiveCode(), normalized)
                || contains(question.getStemText(), normalized)
                || contains(question.getKnowledgeTags(), normalized)
                || contains(question.getChapterName(), normalized)
                || contains(question.getSubmitterName(), normalized)
                || contains(question.getSourceOrg(), normalized);
    }

    private boolean matchStatus(Question question, String status) {
        if (!StringUtils.hasText(status) || "ALL".equalsIgnoreCase(status)) {
            return true;
        }
        return reviewStatusOf(question).name().equalsIgnoreCase(status);
    }

    private boolean matchUserKeyword(AppUser user, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return true;
        }
        return contains(user.getUsername(), keyword)
                || contains(user.getDisplayName(), keyword)
                || contains(user.getEmail(), keyword)
                || contains(user.getPhone(), keyword);
    }

    private boolean contains(String source, String keyword) {
        return source != null && source.toLowerCase(Locale.ROOT).contains(keyword);
    }

    private String normalizeKeyword(String keyword) {
        return StringUtils.hasText(keyword) ? keyword.trim().toLowerCase(Locale.ROOT) : null;
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String format(LocalDateTime value) {
        return value == null ? "--" : value.format(DATE_TIME_FORMATTER);
    }

    private String defaultIfBlank(String value, String fallback) {
        return StringUtils.hasText(value) ? value.trim() : fallback;
    }

    private ReviewStatus reviewStatusOf(Question question) {
        return question.getReviewStatus() == null ? ReviewStatus.APPROVED : question.getReviewStatus();
    }

    private UserRole parseRole(String role) {
        if (!StringUtils.hasText(role)) {
            throw new IllegalArgumentException("角色不能为空");
        }
        try {
            return UserRole.valueOf(role.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("不支持的角色类型");
        }
    }
}
