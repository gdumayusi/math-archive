package com.example.matharchive.service;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.example.matharchive.domain.AppUser;
import com.example.matharchive.domain.UserRole;
import com.example.matharchive.dto.LoginRequest;
import com.example.matharchive.dto.LoginResponse;
import com.example.matharchive.dto.RegisterRequest;
import com.example.matharchive.repository.AppUserRepository;

@Service
public class AuthService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private final AppUserRepository appUserRepository;

    public AuthService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        String username = request.username().trim();
        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("账号不存在"));

        if (!user.isActive()) {
            throw new IllegalArgumentException("该账号已被停用");
        }
        if (!user.getPassword().equals(request.password())) {
            throw new IllegalArgumentException("密码错误");
        }

        user.setLastLoginAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return buildLoginResponse(appUserRepository.save(user));
    }

    @Transactional
    public LoginResponse register(RegisterRequest request) {
        String username = request.username().trim();
        String displayName = request.displayName().trim();
        String email = StringUtils.hasText(request.email()) ? request.email().trim() : null;
        LocalDateTime now = LocalDateTime.now();

        if (appUserRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("用户名已存在，请更换后重试");
        }
        if (StringUtils.hasText(email) && !EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("邮箱格式不正确");
        }

        AppUser user = new AppUser();
        user.setUsername(username);
        user.setDisplayName(displayName);
        user.setPassword(request.password());
        user.setEmail(email);
        user.setStudentNo(generateStudentNo());
        user.setAvatarKey("classic");
        user.setBio("这个用户还没有填写个人简介。");
        user.setRole(UserRole.USER);
        user.setActive(true);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        user.setLastLoginAt(now);

        return buildLoginResponse(appUserRepository.save(user));
    }

    private LoginResponse buildLoginResponse(AppUser user) {
        return new LoginResponse(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getStudentNo(),
                user.getRole().name(),
                user.getAvatarKey(),
                user.getAvatarImage(),
                "demo-token-" + user.getId()
        );
    }

    private String generateStudentNo() {
        return "2023" + String.format("%06d", appUserRepository.count() + 1);
    }
}
