package com.example.matharchive.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.matharchive.domain.SystemSetting;

public interface SystemSettingRepository extends JpaRepository<SystemSetting, String> {
}
