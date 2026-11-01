package jp.drmatching.researcher;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/researcher-registrations")
public class ResearcherRegistrationController {
    private final ResearcherRegistrationService service;

    public ResearcherRegistrationController(ResearcherRegistrationService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ResearcherRegistrationResponse> register(
            @Valid @RequestBody ResearcherRegistrationRequest request) {
        return ResponseEntity.accepted().body(service.register(request));
    }
}
