package com.piiscan.pii.entity;

import com.piiscan.pii.enums.PiiCategory;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "custom_patterns")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomPattern {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String patternName;

    @Enumerated(EnumType.STRING)
    private PiiCategory category;

    @Column(nullable = false, length = 500)
    private String regex;

    @Column(length = 500)
    private String description;

    @Builder.Default
    private boolean enabled = true;

    private String createdBy;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
