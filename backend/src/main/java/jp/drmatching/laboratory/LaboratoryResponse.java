package jp.drmatching.laboratory;

import java.time.OffsetDateTime;

public record LaboratoryResponse(
        Long id,
        String universityName,
        String graduateSchoolName,
        String laboratoryName,
        String supervisorName,
        String researchField,
        String researchSummary,
        boolean acceptsWorkingProfessionals,
        OffsetDateTime createdAt) {}
