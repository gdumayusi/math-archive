package com.example.matharchive.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "category_taxonomy")
public class CategoryTaxonomy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 32)
    private String subject;

    @Column(nullable = false)
    private Integer subjectOrder;

    @Column(nullable = false, length = 128)
    private String chapterName;

    @Column(nullable = false)
    private Integer chapterOrder;

    @Column(length = 128)
    private String knowledgeTag;

    @Column
    private Integer knowledgeOrder;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Integer getSubjectOrder() {
        return subjectOrder;
    }

    public void setSubjectOrder(Integer subjectOrder) {
        this.subjectOrder = subjectOrder;
    }

    public String getChapterName() {
        return chapterName;
    }

    public void setChapterName(String chapterName) {
        this.chapterName = chapterName;
    }

    public Integer getChapterOrder() {
        return chapterOrder;
    }

    public void setChapterOrder(Integer chapterOrder) {
        this.chapterOrder = chapterOrder;
    }

    public String getKnowledgeTag() {
        return knowledgeTag;
    }

    public void setKnowledgeTag(String knowledgeTag) {
        this.knowledgeTag = knowledgeTag;
    }

    public Integer getKnowledgeOrder() {
        return knowledgeOrder;
    }

    public void setKnowledgeOrder(Integer knowledgeOrder) {
        this.knowledgeOrder = knowledgeOrder;
    }
}
