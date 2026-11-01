package jp.drmatching.admin;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotBlank;

public record RegistrationDecisionRequest(
        @NotBlank @Pattern(regexp = "approved|rejected") String decision) {}
