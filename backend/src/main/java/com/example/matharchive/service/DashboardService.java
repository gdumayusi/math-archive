package com.example.matharchive.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.matharchive.domain.AppUser;
import com.example.matharchive.domain.PracticeStatus;
import com.example.matharchive.domain.UserRole;
import com.example.matharchive.dto.DashboardResponse;
import com.example.matharchive.dto.ResumeCardDto;
import com.example.matharchive.dto.StatCardDto;
import com.example.matharchive.repository.AppUserRepository;
import com.example.matharchive.repository.FavoriteRepository;
import com.example.matharchive.repository.MistakeRecordRepository;
import com.example.matharchive.repository.PracticeRecordRepository;
import com.example.matharchive.repository.QuestionRepository;

@Service
public class DashboardService {

    private final AppUserRepository appUserRepository;
    private final QuestionRepository questionRepository;
    private final FavoriteRepository favoriteRepository;
    private final MistakeRecordRepository mistakeRecordRepository;
    private final PracticeRecordRepository practiceRecordRepository;

    public DashboardService(
            AppUserRepository appUserRepository,
            QuestionRepository questionRepository,
            FavoriteRepository favoriteRepository,
            MistakeRecordRepository mistakeRecordRepository,
            PracticeRecordRepository practiceRecordRepository
    ) {
        this.appUserRepository = appUserRepository;
        this.questionRepository = questionRepository;
        this.favoriteRepository = favoriteRepository;
        this.mistakeRecordRepository = mistakeRecordRepository;
        this.practiceRecordRepository = practiceRecordRepository;
    }

    public DashboardResponse getDashboard(Long userId) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        if (user.getRole() == UserRole.ADMIN) {
            return new DashboardResponse(
                    "ADMIN",
                    "系统运行总览",
                    "聚焦题库沉淀、用户活跃与知识点覆盖情况",
                    List.of(
                            new StatCardDto("题库总量", String.valueOf(questionRepository.count()), "当前归档真题总数", "database", false),
                            new StatCardDto("分类模块", String.valueOf(Math.max(1, questionRepository.count() / 2)), "围绕知识点持续扩充", "book", false),
                            new StatCardDto("用户账号", String.valueOf(appUserRepository.count()), "含管理员与普通考生", "users", false),
                            new StatCardDto("错题记录", String.valueOf(mistakeRecordRepository.count()), "用于追踪复盘热点", "alert", true)
                    ),
                    new ResumeCardDto("进入用户管理", "继续维护账号状态、角色权限与学习行为数据。", "前往管理", "/admin/users")
            );
        }

        long totalPractices = practiceRecordRepository.countByUser_Id(user.getId());
        long masteredCount = practiceRecordRepository.countByUser_IdAndStatus(user.getId(), PracticeStatus.MASTERED);

        return new DashboardResponse(
                "USER",
                "学习概览：" + user.getDisplayName(),
                "围绕真题练习、错题回顾与知识点掌握持续复盘",
                List.of(
                        new StatCardDto("练习记录", String.valueOf(totalPractices), "已记录的刷题行为", "book", false),
                        new StatCardDto("错题档案", String.valueOf(mistakeRecordRepository.countByUser_Id(user.getId())), "建议优先回看未掌握题目", "alert", true),
                        new StatCardDto("收藏题目", String.valueOf(favoriteRepository.countByUser_Id(user.getId())), "可用于二刷与集中整理", "bookmark", false),
                        new StatCardDto("掌握题次", String.valueOf(masteredCount), "记录为已掌握的练习结果", "barChart", false)
                ),
                new ResumeCardDto("查看学习统计", "现在可以查看近 7 天趋势、薄弱知识点和最近练习记录。", "进入统计", "/study-stats")
        );
    }
}
