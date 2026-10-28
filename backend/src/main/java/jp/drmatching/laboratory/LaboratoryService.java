package jp.drmatching.laboratory;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class LaboratoryService {
    private final LaboratoryRepository repository;

    public LaboratoryService(LaboratoryRepository repository) {
        this.repository = repository;
    }

    public List<LaboratoryResponse> findAll() {
        return repository.findAll().stream().map(Laboratory::toResponse).toList();
    }

    public List<LaboratoryResponse> search(String keyword) {
        String normalized = keyword == null ? "" : keyword.strip();
        if (normalized.isEmpty()) return findAll();
        String escaped = normalized.replace("!", "!!").replace("%", "!%").replace("_", "!_");
        return repository.search(escaped).stream().map(Laboratory::toResponse).toList();
    }

    @Transactional
    public LaboratoryResponse create(LaboratoryRequest request) {
        return repository.save(new Laboratory(request)).toResponse();
    }

    @Transactional
    public LaboratoryResponse update(Long id, LaboratoryRequest request) {
        Laboratory laboratory = repository.findById(id).orElseThrow(() -> new LaboratoryNotFoundException(id));
        laboratory.update(request);
        return repository.save(laboratory).toResponse();
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new LaboratoryNotFoundException(id);
        }
        repository.deleteById(id);
    }
}
