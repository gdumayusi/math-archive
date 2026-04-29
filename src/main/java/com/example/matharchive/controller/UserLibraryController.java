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
import org.springframework.web.bind.annotation.RestController;

import com.example.matharchive.dto.PasswordChangeRequest;
import com.example.matharchive.dto.PracticeRecordRequest;
import com.example.matharchive.dto.QuestionItemDto;
import com.example.matharchive.dto.StudyStatsResponse;
import com.example.matharchive.dto.UserProfileResponse;
import com.example.matharchive.dto.UserProfileUpdateRequest;
import com.example.matharchive.service.QuestionService;
import com.example.matharchive.service.StudyService;
import com.example.matharchive.service.UserProfileService;

@RestController
@RequestMapping("/api/users/{userId}")
public class UserLibraryController {

    private final QuestionService questionService;
    private final StudyService studyService;
    private final UserProfileService userProfileService;

    public UserLibraryController(
            QuestionService questionService,
            StudyService studyService,
            UserProfileService userProfileService
    ) {
        this.questionService = questionService;
        this.studyService = studyService;
        this.userProfileService = userProfileService;
    }

    @GetMapping("/profile")
    public UserProfileResponse getProfile(@PathVariable Long userId) {
        return userProfileService.getProfile(userId);
    }

    @PutMapping("/profile")
    public UserProfileResponse updateProfile(
            @PathVariable Long userId,
            @Validated @RequestBody UserProfileUpdateRequest request
    ) {
        return userProfileService.updateProfile(userId, request);
    }

    @PutMapping("/password")
    public Map<String, String> changePassword(
            @PathVariable Long userId,
            @Validated @RequestBody PasswordChangeRequest request
    ) {
        userProfileService.changePassword(userId, request);
        return Map.of("message", "Password updated");
    }

    @GetMapping("/favorites")
    public List<QuestionItemDto> getFavorites(@PathVariable Long userId) {
        return questionService.getFavorites(userId);
    }

    @PostMapping("/favorites/{questionId}")
    public Map<String, String> addFavorite(@PathVariable Long userId, @PathVariable Long questionId) {
        questionService.addFavorite(userId, questionId);
        return Map.of("message", "Favorite added");
    }

    @DeleteMapping("/favorites/{questionId}")
    public Map<String, String> removeFavorite(@PathVariable Long userId, @PathVariable Long questionId) {
        questionService.removeFavorite(userId, questionId);
        return Map.of("message", "Favorite removed");
    }

    @GetMapping("/mistakes")
    public List<QuestionItemDto> getMistakes(@PathVariable Long userId) {
        return questionService.getMistakes(userId);
    }

    @PostMapping("/mistakes/{questionId}")
    public Map<String, String> addMistake(
            @PathVariable Long userId,
            @PathVariable Long questionId,
            @RequestBody(required = false) Map<String, String> payload
    ) {
        String note = payload == null ? null : payload.get("note");
        questionService.addMistake(userId, questionId, note);
        return Map.of("message", "Mistake added");
    }

    @DeleteMapping("/mistakes/{questionId}")
    public Map<String, String> removeMistake(@PathVariable Long userId, @PathVariable Long questionId) {
        questionService.removeMistake(userId, questionId);
        return Map.of("message", "Mistake removed");
    }

    @PostMapping("/practice-records")
    public Map<String, String> recordPractice(
            @PathVariable Long userId,
            @Validated @RequestBody PracticeRecordRequest request
    ) {
        studyService.recordPractice(userId, request);
        return Map.of("message", "Practice recorded");
    }

    @GetMapping("/study-stats")
    public StudyStatsResponse getStudyStats(@PathVariable Long userId) {
        return studyService.getStudyStats(userId);
    }
}
