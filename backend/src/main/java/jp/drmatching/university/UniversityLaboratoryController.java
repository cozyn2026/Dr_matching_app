package jp.drmatching.university;

import jakarta.validation.Valid;
import java.util.List;
import jp.drmatching.laboratory.LaboratoryRequest;
import jp.drmatching.laboratory.LaboratoryResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/university")
public class UniversityLaboratoryController {
    private final UniversityLaboratoryService service;

    public UniversityLaboratoryController(UniversityLaboratoryService service) {
        this.service = service;
    }

    @GetMapping("/laboratories")
    public List<LaboratoryResponse> findMyLaboratories(Authentication authentication) {
        return service.findMyLaboratories(authentication.getName());
    }

    @PostMapping("/laboratories")
    public ResponseEntity<LaboratoryResponse> create(
            @Valid @RequestBody LaboratoryRequest request, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.create(authentication.getName(), request));
    }

    @PutMapping("/laboratories/{id}")
    public LaboratoryResponse update(
            @PathVariable Long id, @Valid @RequestBody LaboratoryRequest request,
            Authentication authentication) {
        return service.update(authentication.getName(), id, request);
    }

    @DeleteMapping("/laboratories/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        service.delete(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/interested-researchers")
    public List<UniversityResearcherInterest> findInterestedResearchers(Authentication authentication) {
        return service.findInterestedResearchers(authentication.getName());
    }

    @GetMapping("/notifications")
    public List<UniversityNotification> findNotifications(Authentication authentication) {
        return service.findNotifications(authentication.getName());
    }

    @PutMapping("/notifications/read")
    public ResponseEntity<Void> markNotificationsRead(Authentication authentication) {
        service.markNotificationsRead(authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
