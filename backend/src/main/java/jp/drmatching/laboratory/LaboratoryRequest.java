package jp.drmatching.laboratory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LaboratoryRequest(
        @NotBlank @Size(max = 120) String universityName,
        @NotBlank @Size(max = 160) String graduateSchoolName,
        @NotBlank @Size(max = 160) String laboratoryName,
        @NotBlank @Size(max = 120) String supervisorName,
        @NotBlank @Size(max = 100) String researchField,
        @NotBlank @Size(max = 3000) String researchSummary,
        boolean acceptsWorkingProfessionals) {}
