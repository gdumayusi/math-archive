package com.example.matharchive.controller;

import java.util.List;
import java.util.Map;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
import com.example.matharchive.service.AdminService;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/overview")
    public AdminOverviewDto getOverview() {
        return adminService.getOverview();
    }

    @GetMapping("/questions")
    public PageResponse<QuestionItemDto> getQuestions(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size
    ) {
        return adminService.getQuestions(keyword, page, size);
    }

    @PostMapping("/questions")
    public QuestionItemDto createQuestion(@Validated @RequestBody QuestionUpsertRequest request) {
        return adminService.createQuestion(request);
    }

    @PutMapping("/questions/{id}")
    public QuestionItemDto updateQuestion(@PathVariable Long id, @Validated @RequestBody QuestionUpsertRequest request) {
        return adminService.updateQuestion(id, request);
    }

    @DeleteMapping("/questions/{id}")
    public Map<String, String> deleteQuestion(@PathVariable Long id) {
        adminService.deleteQuestion(id);
        return Map.of("message", "删除成功");
    }

    @GetMapping("/reviews")
    public PageResponse<AdminQuestionReviewDto> getQuestionReviews(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "ALL") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size
    ) {
        return adminService.getQuestionReviews(keyword, status, page, size);
    }

    @PostMapping("/reviews/{id}/action")
    public AdminQuestionReviewDto reviewQuestion(
            @PathVariable Long id,
            @Validated @RequestBody AdminReviewActionRequest request
    ) {
        return adminService.reviewQuestion(id, request);
    }

    @GetMapping("/users")
    public List<UserRowDto> getUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Boolean active
    ) {
        return adminService.getUsers(keyword, role, active);
    }

    @PostMapping("/users")
    public UserRowDto createUser(@Validated @RequestBody UserUpsertRequest request) {
        return adminService.createUser(request);
    }

    @PutMapping("/users/{id}")
    public UserRowDto updateUser(@PathVariable Long id, @Validated @RequestBody UserUpsertRequest request) {
        return adminService.updateUser(id, request);
    }

    @DeleteMapping("/users/{id}")
    public Map<String, String> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return Map.of("message", "删除成功");
    }

    @GetMapping("/stats")
    public AdminDataStatsDto getDataStats() {
        return adminService.getDataStats();
    }

    @GetMapping("/settings")
    public AdminSystemSettingsDto getSystemSettings() {
        return adminService.getSystemSettings();
    }

    @PutMapping("/settings")
    public AdminSystemSettingsDto updateSystemSettings(
            @Validated @RequestBody AdminSystemSettingsUpdateRequest request
    ) {
        return adminService.updateSystemSettings(request);
    }

    @GetMapping("/logs")
    public List<OperationLogDto> getOperationLogs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String module
    ) {
        return adminService.getOperationLogs(keyword, module);
    }
}
