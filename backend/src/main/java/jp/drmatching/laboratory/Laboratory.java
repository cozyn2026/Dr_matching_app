package jp.drmatching.laboratory;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "laboratories")
public class Laboratory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "university_name", nullable = false, length = 120)
    private String universityName;

    @Column(name = "graduate_school_name", nullable = false, length = 160)
    private String graduateSchoolName;

    @Column(name = "laboratory_name", nullable = false, length = 160)
    private String laboratoryName;

    @Column(name = "supervisor_name", nullable = false, length = 120)
    private String supervisorName;

    @Column(name = "research_field", nullable = false, length = 100)
    private String researchField;

    @Column(name = "research_summary", nullable = false, columnDefinition = "text")
    private String researchSummary;

    @Column(name = "accepts_working_professionals", nullable = false)
    private boolean acceptsWorkingProfessionals;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected Laboratory() {}

    public Laboratory(LaboratoryRequest request) {
        universityName = request.universityName();
        graduateSchoolName = request.graduateSchoolName();
        laboratoryName = request.laboratoryName();
        supervisorName = request.supervisorName();
        researchField = request.researchField();
        researchSummary = request.researchSummary();
        acceptsWorkingProfessionals = request.acceptsWorkingProfessionals();
    }

    public void update(LaboratoryRequest request) {
        universityName = request.universityName();
        graduateSchoolName = request.graduateSchoolName();
        laboratoryName = request.laboratoryName();
        supervisorName = request.supervisorName();
        researchField = request.researchField();
        researchSummary = request.researchSummary();
        acceptsWorkingProfessionals = request.acceptsWorkingProfessionals();
    }

    public LaboratoryResponse toResponse() {
        return new LaboratoryResponse(id, universityName, graduateSchoolName, laboratoryName,
                supervisorName, researchField, researchSummary, acceptsWorkingProfessionals, createdAt);
    }
}
