package jp.drmatching.university;

import java.time.OffsetDateTime;

public record UniversityResearcherInterest(
        Long laboratoryId,
        String laboratoryName,
        String displayName,
        String currentPosition,
        String careerSummary,
        String researchSummary,
        String preferredDegreeField,
        String preferredStartTerm,
        String preferredStudyMode,
        String preferredRegion,
        String workBalanceConditions,
        OffsetDateTime favoritedAt) {}
