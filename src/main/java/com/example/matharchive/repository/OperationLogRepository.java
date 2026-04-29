package com.example.matharchive.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.matharchive.domain.OperationLog;

public interface OperationLogRepository extends JpaRepository<OperationLog, Long> {
}
