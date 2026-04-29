package com.example.matharchive.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.matharchive.domain.Favorite;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    long countByUser_Id(Long userId);

    boolean existsByUser_IdAndQuestion_Id(Long userId, Long questionId);

    void deleteByUser_IdAndQuestion_Id(Long userId, Long questionId);

    void deleteByUser_Id(Long userId);

    void deleteByQuestion_Id(Long questionId);

    @EntityGraph(attributePaths = {"question"})
    List<Favorite> findByUser_IdOrderByCreatedAtDesc(Long userId);
}
