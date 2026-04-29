package com.example.matharchive.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.matharchive.domain.PracticeRecord;
import com.example.matharchive.domain.PracticeStatus;

public interface PracticeRecordRepository extends JpaRepository<PracticeRecord, Long> {

    long countByUser_Id(Long userId);

    long countByUser_IdAndStatus(Long userId, PracticeStatus status);

    Optional<PracticeRecord> findFirstByUser_IdOrderByPracticedAtDesc(Long userId);

    @Query("""
            SELECT pr
            FROM PracticeRecord pr
            JOIN FETCH pr.question q
            WHERE pr.user.id = :userId
              AND q.id = :questionId
            ORDER BY pr.practicedAt DESC, pr.id DESC
            """)
    List<PracticeRecord> findLatestByUserIdAndQuestionId(@Param("userId") Long userId, @Param("questionId") Long questionId);

    @Query("""
            SELECT pr
            FROM PracticeRecord pr
            JOIN FETCH pr.question q
            WHERE pr.user.id = :userId
            ORDER BY q.id ASC, pr.practicedAt DESC, pr.id DESC
            """)
    List<PracticeRecord> findByUser_IdOrderByQuestion_IdAscPracticedAtDescIdDesc(@Param("userId") Long userId);

    @Query("""
            SELECT pr
            FROM PracticeRecord pr
            JOIN FETCH pr.question q
            WHERE pr.user.id = :userId
              AND q.id IN :questionIds
            ORDER BY q.id ASC, pr.practicedAt DESC, pr.id DESC
            """)
    List<PracticeRecord> findByUser_IdAndQuestion_IdInOrderByQuestion_IdAscPracticedAtDescIdDesc(
            @Param("userId") Long userId,
            @Param("questionIds") Collection<Long> questionIds
    );

    void deleteByUser_Id(Long userId);

    void deleteByQuestion_Id(Long questionId);
}
