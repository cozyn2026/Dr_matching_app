package jp.drmatching.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResearcherLoginRequest(
        @NotBlank @Email @Size(max = 254) String email,
        @NotBlank String password) {}
