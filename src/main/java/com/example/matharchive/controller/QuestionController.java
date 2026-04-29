package com.example.matharchive.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.matharchive.dto.CategoryNodeDto;
import com.example.matharchive.dto.PageResponse;
import com.example.matharchive.dto.QuestionDetailDto;
import com.example.matharchive.dto.QuestionItemDto;
import com.example.matharchive.service.KnowledgeService;
import com.example.matharchive.service.QuestionService;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    private final QuestionService questionService;
    private final KnowledgeService knowledgeService;

    public QuestionController(QuestionService questionService, KnowledgeService knowledgeService) {
        this.questionService = questionService;
        this.knowledgeService = knowledgeService;
    }

    @GetMapping
    public PageResponse<QuestionItemDto> searchQuestions(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String subjects,
            @RequestParam(required = false) String chapterNames,
            @RequestParam(required = false) String knowledgeTags,
            @RequestParam(required = false) String paperNames,
            @RequestParam(required = false) String years,
            @RequestParam(required = false) String questionTypes,
            @RequestParam(required = false) String difficulties,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size,
            @RequestParam(defaultValue = "latest") String sort
    ) {
        return questionService.searchQuestions(
                userId,
                keyword,
                subjects,
                chapterNames,
                knowledgeTags,
                paperNames,
                years,
                questionTypes,
                difficulties,
                page,
                size,
                sort
        );
    }

    @GetMapping("/{id}")
    public QuestionDetailDto getQuestionDetail(
            @PathVariable Long id,
            @RequestParam(required = false) Long userId
    ) {
        return questionService.getQuestionDetail(id, userId);
    }

    @GetMapping("/taxonomy")
    public List<CategoryNodeDto> getQuestionTaxonomy() {
        return knowledgeService.getCategoryTree();
    }
}
