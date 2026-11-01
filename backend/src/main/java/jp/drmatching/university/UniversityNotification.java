package jp.drmatching.university;

import java.time.OffsetDateTime;

public record UniversityNotification(
        Long id,
        String researcherName,
        Long laboratoryId,
        String laboratoryName,
        OffsetDateTime createdAt,
        boolean read) {}
