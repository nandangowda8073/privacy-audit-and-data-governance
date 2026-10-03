package com.piiscan.pii.repository;

import com.piiscan.pii.entity.CustomPattern;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomPatternRepository extends JpaRepository<CustomPattern, Long> {
    List<CustomPattern> findByEnabledTrue();
    boolean existsByPatternName(String patternName);
}
