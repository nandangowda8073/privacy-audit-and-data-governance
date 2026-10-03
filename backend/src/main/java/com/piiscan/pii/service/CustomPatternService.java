package com.piiscan.pii.service;

import com.piiscan.pii.dto.CustomPatternDto;
import com.piiscan.pii.entity.CustomPattern;
import com.piiscan.pii.exception.PatternException;
import com.piiscan.pii.repository.CustomPatternRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomPatternService {

    private final CustomPatternRepository repository;

    public List<CustomPattern> getAll() {
        return repository.findAll();
    }

    public CustomPattern create(CustomPatternDto dto, String createdBy) {
        if (repository.existsByPatternName(dto.getPatternName())) {
            throw new PatternException("Pattern with name '" + dto.getPatternName() + "' already exists.");
        }
        validateRegex(dto.getRegex());

        CustomPattern pattern = CustomPattern.builder()
            .patternName(dto.getPatternName())
            .category(dto.getCategory())
            .regex(dto.getRegex())
            .description(dto.getDescription())
            .enabled(dto.isEnabled())
            .createdBy(createdBy)
            .build();

        CustomPattern saved = repository.save(pattern);
        log.info("Custom pattern '{}' created by '{}'", saved.getPatternName(), createdBy);
        return saved;
    }

    public CustomPattern update(Long id, CustomPatternDto dto) {
        CustomPattern existing = repository.findById(id)
            .orElseThrow(() -> new PatternException("Pattern with id " + id + " not found."));
        validateRegex(dto.getRegex());

        existing.setPatternName(dto.getPatternName());
        existing.setCategory(dto.getCategory());
        existing.setRegex(dto.getRegex());
        existing.setDescription(dto.getDescription());
        existing.setEnabled(dto.isEnabled());

        return repository.save(existing);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new PatternException("Pattern with id " + id + " not found.");
        }
        repository.deleteById(id);
        log.info("Custom pattern id={} deleted", id);
    }

    private void validateRegex(String regex) {
        try {
            Pattern.compile(regex);
        } catch (PatternSyntaxException e) {
            throw new PatternException("Invalid regex: " + e.getMessage(), e);
        }
    }
}
