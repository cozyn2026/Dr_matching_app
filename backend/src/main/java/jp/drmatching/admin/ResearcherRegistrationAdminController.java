package jp.drmatching.admin;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/researcher-registrations")
public class ResearcherRegistrationAdminController {
    private final AdminApiKeyService apiKeyService;
    private final ResearcherRegistrationAdminService service;

    public ResearcherRegistrationAdminController(
            AdminApiKeyService apiKeyService, ResearcherRegistrationAdminService service) {
        this.apiKeyService = apiKeyService;
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<PendingResearcherRegistration>> findForReview(
            @RequestHeader(value = "X-Admin-Key", required = false) String adminKey) {
        apiKeyService.requireValid(adminKey);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(service.findForReview());
    }

    @PatchMapping("/{membershipId}")
    public RegistrationReviewResponse decide(
            @PathVariable Long membershipId,
            @RequestHeader(value = "X-Admin-Key", required = false) String adminKey,
            @Valid @RequestBody RegistrationDecisionRequest request) {
        apiKeyService.requireValid(adminKey);
        return service.decide(membershipId, request.decision());
    }
}
