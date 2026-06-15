package com.zebacodes.dbstress.repository;

import com.zebacodes.dbstress.model.StressTestResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface StressTestResultRepository extends JpaRepository<StressTestResult, UUID> {

    List<StressTestResult> findTop10ByOrderByStartedAtDesc();
}
