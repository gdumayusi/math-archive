package com.example.matharchive.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.matharchive.domain.CategoryTaxonomy;

public interface CategoryTaxonomyRepository extends JpaRepository<CategoryTaxonomy, Long> {

    List<CategoryTaxonomy> findAllByOrderBySubjectOrderAscChapterOrderAscKnowledgeOrderAscIdAsc();

    boolean existsBySubjectAndChapterNameAndKnowledgeTagIsNull(String subject, String chapterName);

    boolean existsBySubjectAndChapterNameAndKnowledgeTag(String subject, String chapterName, String knowledgeTag);
}
