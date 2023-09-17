package com.jurewicz.jpaalternatives.domain;

import java.time.LocalDateTime;

public record Comment(Long id, String name, String content, LocalDateTime createdAt, LocalDateTime updatedAt) {
}