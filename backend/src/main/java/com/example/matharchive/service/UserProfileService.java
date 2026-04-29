package com.example.matharchive.service;

import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.example.matharchive.domain.AppUser;
import com.example.matharchive.domain.PracticeRecord;
import com.example.matharchive.domain.PracticeStatus;
import com.example.matharchive.dto.PasswordChangeRequest;
import com.example.matharchive.dto.UserProfileResponse;
import com.example.matharchive.dto.UserProfileUpdateRequest;
import com.example.matharchive.repository.AppUserRepository;
import com.example.matharchive.repository.FavoriteRepository;
import com.example.matharchive.repository.MistakeRecordRepository;
import com.example.matharchive.repository.PracticeRecordRepository;

@Service
public class UserProfileService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Set<String> AVATAR_KEYS = Set.of("classic", "ocean", "sunrise", "forest", "plum", "night");
    private static final int MAX_AVATAR_IMAGE_LENGTH = 2_000_000;

    private final AppUserRepository appUserRepository;
    private final FavoriteRepository favoriteRepository;
    private final MistakeRecordRepository mistakeRecordRepository;
    private final PracticeRecordRepository practiceRecordRepository;

    public UserProfileService(
            AppUserRepository appUserRepository,
            FavoriteRepository favoriteRepository,
            MistakeRecordRepository mistakeRecordRepository,
            PracticeRecordRepository practiceRecordRepository
    ) {
        this.appUserRepository = appUserRepository;
        this.favoriteRepository = favoriteRepository;
        this.mistakeRecordRepository = mistakeRecordRepository;
        this.practiceRecordRepository = practiceRecordRepository;
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(Long userId) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        long practiceCount = practiceRecordRepository.countByUser_Id(userId);
        PracticeRecord latestPractice = practiceRecordRepository.findFirstByUser_IdOrderByPracticedAtDesc(userId)
                .orElse(null);

        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getEmail(),
                user.getStudentNo(),
                user.getAvatarKey(),
                user.getAvatarImage(),
                user.getBio(),
                user.getRole().name(),
                user.isActive(),
                user.getCreatedAt().format(DATE_TIME_FORMATTER),
                user.getUpdatedAt().format(DATE_TIME_FORMATTER),
                latestPractice == null ? "暂无练习记录" : latestPractice.getPracticedAt().format(DATE_TIME_FORMATTER),
                safeToInt(favoriteRepository.countByUser_Id(userId)),
                safeToInt(mistakeRecordRepository.countByUser_Id(userId)),
                safeToInt(practiceCount),
                safeToInt(practiceRecordRepository.countByUser_IdAndStatus(userId, PracticeStatus.MASTERED))
        );
    }

    @Transactional
    public UserProfileResponse updateProfile(Long userId, UserProfileUpdateRequest request) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        String displayName = request.displayName().trim();
        String email = trimToNull(request.email());
        String bio = trimToNull(request.bio());
        String avatarKey = StringUtils.hasText(request.avatarKey()) ? request.avatarKey().trim().toLowerCase() : "classic";
        String avatarImage = trimToNull(request.avatarImage());

        if (StringUtils.hasText(email) && !EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("邮箱格式不正确");
        }
        if (!AVATAR_KEYS.contains(avatarKey)) {
            throw new IllegalArgumentException("头像类型不支持");
        }
        if (StringUtils.hasText(avatarImage)) {
            if (!avatarImage.startsWith("data:image/")) {
                throw new IllegalArgumentException("头像图片格式不正确");
            }
            if (avatarImage.length() > MAX_AVATAR_IMAGE_LENGTH) {
                throw new IllegalArgumentException("头像图片过大，请重新选择");
            }
        }

        user.setDisplayName(displayName);
        user.setEmail(email);
        user.setBio(bio);
        user.setAvatarKey(avatarKey);
        user.setAvatarImage(avatarImage);
        user.setUpdatedAt(java.time.LocalDateTime.now());
        appUserRepository.save(user);
        return getProfile(userId);
    }

    @Transactional
    public void changePassword(Long userId, PasswordChangeRequest request) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        if (!user.getPassword().equals(request.currentPassword())) {
            throw new IllegalArgumentException("当前密码不正确");
        }
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new IllegalArgumentException("两次输入的新密码不一致");
        }
        if (request.newPassword().trim().length() < 6) {
            throw new IllegalArgumentException("新密码长度不能少于6位");
        }

        user.setPassword(request.newPassword());
        user.setUpdatedAt(java.time.LocalDateTime.now());
        appUserRepository.save(user);
    }

    private int safeToInt(long value) {
        return Math.toIntExact(Math.min(value, Integer.MAX_VALUE));
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
