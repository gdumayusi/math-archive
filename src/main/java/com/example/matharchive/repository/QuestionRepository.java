package com.example.matharchive.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.example.matharchive.domain.Question;

public interface QuestionRepository extends JpaRepository<Question, Long>, JpaSpecificationExecutor<Question> {

    boolean existsByArchiveCode(String archiveCode);

    List<Question> findByArchiveCodeStartingWith(String prefix);
}
