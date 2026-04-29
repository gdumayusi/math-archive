package com.example.matharchive.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.example.matharchive.domain.CategoryTaxonomy;
import com.example.matharchive.domain.Question;
import com.example.matharchive.dto.CategoryNodeDto;
import com.example.matharchive.repository.CategoryTaxonomyRepository;
import com.example.matharchive.repository.QuestionRepository;

@Service
public class KnowledgeService {

    private final QuestionRepository questionRepository;
    private final CategoryTaxonomyRepository categoryTaxonomyRepository;

    public KnowledgeService(
            QuestionRepository questionRepository,
            CategoryTaxonomyRepository categoryTaxonomyRepository
    ) {
        this.questionRepository = questionRepository;
        this.categoryTaxonomyRepository = categoryTaxonomyRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryNodeDto> getCategoryTree() {
        List<CategoryTaxonomy> taxonomyEntries = categoryTaxonomyRepository.findAllByOrderBySubjectOrderAscChapterOrderAscKnowledgeOrderAscIdAsc();
        if (!taxonomyEntries.isEmpty()) {
            return buildTreeFromTaxonomy(taxonomyEntries);
        }
        return buildTreeFromQuestions();
    }

    private List<CategoryNodeDto> buildTreeFromTaxonomy(List<CategoryTaxonomy> taxonomyEntries) {
        Map<String, Integer> subjectCounts = new HashMap<>();
        Map<String, Integer> chapterCounts = new HashMap<>();
        Map<String, Integer> knowledgeCounts = new HashMap<>();

        for (Question question : questionRepository.findAll()) {
            subjectCounts.merge(question.getSubject(), 1, Integer::sum);
            chapterCounts.merge(taxonomyKey(question.getSubject(), question.getChapterName()), 1, Integer::sum);

            for (String tag : splitTags(question.getKnowledgeTags())) {
                knowledgeCounts.merge(taxonomyKey(question.getSubject(), question.getChapterName(), tag), 1, Integer::sum);
            }
        }

        Map<String, Map<String, List<CategoryTaxonomy>>> grouped = new LinkedHashMap<>();
        for (CategoryTaxonomy entry : taxonomyEntries) {
            grouped.computeIfAbsent(entry.getSubject(), ignored -> new LinkedHashMap<>());
            grouped.get(entry.getSubject()).computeIfAbsent(entry.getChapterName(), ignored -> new ArrayList<>()).add(entry);
        }

        List<CategoryNodeDto> result = new ArrayList<>();
        grouped.forEach((subject, chapters) -> {
            List<CategoryNodeDto> chapterChildren = new ArrayList<>();
            chapters.forEach((chapterName, entries) -> {
                List<CategoryNodeDto> knowledgeChildren = entries.stream()
                        .filter(entry -> StringUtils.hasText(entry.getKnowledgeTag()))
                        .map(entry -> new CategoryNodeDto(
                                taxonomyKey(subject, chapterName, entry.getKnowledgeTag()),
                                entry.getKnowledgeTag(),
                                knowledgeCounts.getOrDefault(taxonomyKey(subject, chapterName, entry.getKnowledgeTag()), 0),
                                List.of()
                        ))
                        .toList();

                chapterChildren.add(new CategoryNodeDto(
                        taxonomyKey(subject, chapterName),
                        chapterName,
                        chapterCounts.getOrDefault(taxonomyKey(subject, chapterName), 0),
                        knowledgeChildren
                ));
            });

            result.add(new CategoryNodeDto(
                    subject,
                    subject,
                    subjectCounts.getOrDefault(subject, 0),
                    chapterChildren
            ));
        });

        return result;
    }

    private List<CategoryNodeDto> buildTreeFromQuestions() {
        Map<String, Map<String, Integer>> grouped = new LinkedHashMap<>();
        for (Question question : questionRepository.findAll()) {
            grouped.computeIfAbsent(question.getSubject(), key -> new LinkedHashMap<>());
            Map<String, Integer> chapters = grouped.get(question.getSubject());
            chapters.merge(question.getChapterName(), 1, Integer::sum);
        }

        List<CategoryNodeDto> result = new ArrayList<>();
        grouped.forEach((subject, chapters) -> {
            List<CategoryNodeDto> children = chapters.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed().thenComparing(Map.Entry::getKey))
                    .map(entry -> new CategoryNodeDto(
                            taxonomyKey(subject, entry.getKey()),
                            entry.getKey(),
                            entry.getValue(),
                            List.of()
                    ))
                    .toList();

            int total = children.stream().mapToInt(CategoryNodeDto::count).sum();
            result.add(new CategoryNodeDto(subject, subject, total, children));
        });

        result.sort(Comparator.comparing(CategoryNodeDto::label));
        return result;
    }

    private List<String> splitTags(String raw) {
        if (!StringUtils.hasText(raw)) {
            return List.of();
        }
        return List.of(raw.split("[,，]")).stream()
                .map(String::trim)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
    }

    private String taxonomyKey(String subject, String chapterName) {
        return subject + "::" + chapterName;
    }

    private String taxonomyKey(String subject, String chapterName, String knowledgeTag) {
        return subject + "::" + chapterName + "::" + knowledgeTag;
    }
}
