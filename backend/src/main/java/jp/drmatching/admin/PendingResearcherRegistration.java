package jp.drmatching.admin;

import java.time.OffsetDateTime;

public record PendingResearcherRegistration(
        Long membershipId,
        String membershipStatus,
        String displayName,
        String email,
        String organizationName,
        String currentPosition,
        String careerSummary,
        String researchSummary,
        String preferredDegreeField,
        String preferredStartTerm,
        String preferredStudyMode,
        String preferredRegion,
        String workBalanceConditions,
        OffsetDateTime submittedAt) {}
