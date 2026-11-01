package jp.drmatching.researcher;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.AssertTrue;

public record ResearcherRegistrationRequest(
        @NotBlank @Size(max = 200) String organizationName,
        @NotBlank @Size(max = 120) String displayName,
        @NotBlank @Email @Size(max = 254) String email,
        @NotBlank @Size(min = 12, max = 72) String password,
        @NotBlank @Size(max = 160) String currentPosition,
        @NotBlank @Size(max = 3000) String careerSummary,
        @NotBlank @Size(max = 3000) String researchSummary,
        @NotBlank @Size(max = 160) String preferredDegreeField,
        @NotBlank @Size(max = 40) String preferredStartTerm,
        @NotBlank @Size(max = 120) String preferredStudyMode,
        @NotBlank @Size(max = 120) String preferredRegion,
        @NotBlank @Size(max = 3000) String workBalanceConditions,
        @AssertTrue boolean privacyConsent) {}
