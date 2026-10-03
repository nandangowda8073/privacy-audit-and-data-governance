package com.piiscan.scheduler.repository;

import com.piiscan.scheduler.entity.SchedulerConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SchedulerConfigurationRepository extends JpaRepository<SchedulerConfiguration, Long> {
}
