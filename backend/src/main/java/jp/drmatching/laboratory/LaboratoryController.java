package jp.drmatching.laboratory;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import jp.drmatching.admin.AdminApiKeyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/laboratories")
public class LaboratoryController {
    private final LaboratoryService service;
    private final AdminApiKeyService adminApiKeyService;

    public LaboratoryController(LaboratoryService service, AdminApiKeyService adminApiKeyService) {
        this.service = service;
        this.adminApiKeyService = adminApiKeyService;
    }

    @GetMapping
    public List<LaboratoryResponse> findAll(@RequestParam(required = false, defaultValue = "") String keyword) {
        return service.search(keyword);
    }

    @GetMapping("/admin")
    public List<LaboratoryResponse> findAllForAdministration(
            @org.springframework.web.bind.annotation.RequestHeader(value = "X-Admin-Key", required = false)
                    String adminKey) {
        adminApiKeyService.requireValid(adminKey);
        return service.search("");
    }

    @PostMapping
    public ResponseEntity<LaboratoryResponse> create(
            @Valid @RequestBody LaboratoryRequest request,
            @org.springframework.web.bind.annotation.RequestHeader(value = "X-Admin-Key", required = false)
                    String adminKey) {
        adminApiKeyService.requireValid(adminKey);
        LaboratoryResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/api/laboratories/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public LaboratoryResponse update(
            @PathVariable Long id,
            @Valid @RequestBody LaboratoryRequest request,
            @org.springframework.web.bind.annotation.RequestHeader(value = "X-Admin-Key", required = false)
                    String adminKey) {
        adminApiKeyService.requireValid(adminKey);
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @org.springframework.web.bind.annotation.RequestHeader(value = "X-Admin-Key", required = false)
                    String adminKey) {
        adminApiKeyService.requireValid(adminKey);
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
