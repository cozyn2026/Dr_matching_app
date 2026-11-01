package jp.drmatching.university;

import java.util.List;
import jp.drmatching.laboratory.Laboratory;
import jp.drmatching.laboratory.LaboratoryNotFoundException;
import jp.drmatching.laboratory.LaboratoryRepository;
import jp.drmatching.laboratory.LaboratoryRequest;
import jp.drmatching.laboratory.LaboratoryResponse;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UniversityLaboratoryService {
    private final JdbcTemplate jdbc;
    private final LaboratoryRepository laboratories;

    public UniversityLaboratoryService(JdbcTemplate jdbc, LaboratoryRepository laboratories) {
        this.jdbc = jdbc;
        this.laboratories = laboratories;
    }

    @Transactional(readOnly = true)
    public List<LaboratoryResponse> findMyLaboratories(String email) {
        String university = universityName(email);
        return laboratories.findAllByUniversityNameIgnoreCaseOrderByIdAsc(university)
                .stream().map(Laboratory::toResponse).toList();
    }

    @Transactional
    public LaboratoryResponse create(String email, LaboratoryRequest request) {
        String university = universityName(email);
        return laboratories.save(new Laboratory(withUniversity(request, university))).toResponse();
    }

    @Transactional
    public LaboratoryResponse update(String email, Long id, LaboratoryRequest request) {
        String university = universityName(email);
        Laboratory laboratory = laboratories.findByIdAndUniversityNameIgnoreCase(id, university)
                .orElseThrow(() -> new LaboratoryNotFoundException(id));
        laboratory.update(withUniversity(request, university));
        return laboratories.save(laboratory).toResponse();
    }

    @Transactional
    public void delete(String email, Long id) {
        String university = universityName(email);
        Laboratory laboratory = laboratories.findByIdAndUniversityNameIgnoreCase(id, university)
                .orElseThrow(() -> new LaboratoryNotFoundException(id));
        jdbc.update("""
                UPDATE consent_records SET revoked_at = CURRENT_TIMESTAMP
                WHERE consent_type = 'favorite_profile_sharing'
                  AND scope = ? AND revoked_at IS NULL
                """, "laboratory:" + id);
        laboratories.delete(laboratory);
    }

    @Transactional(readOnly = true)
    public List<UniversityResearcherInterest> findInterestedResearchers(String email) {
        String university = universityName(email);
        return jdbc.query("""
                SELECT l.id AS laboratory_id, l.laboratory_name, a.display_name,
                       p.current_position, p.career_summary, p.research_summary,
                       d.preferred_degree_field, d.preferred_start_term, d.preferred_study_mode,
                       d.preferred_region, d.work_balance_conditions, f.created_at AS favorited_at
                FROM laboratories l
                JOIN researcher_favorites f ON f.laboratory_id = l.id
                JOIN accounts a ON a.id = f.account_id AND a.account_status = 'active'
                JOIN consent_records share_consent
                  ON share_consent.account_id = a.id
                 AND share_consent.consent_type = 'favorite_profile_sharing'
                 AND share_consent.scope = 'laboratory:' || l.id::text
                 AND share_consent.revoked_at IS NULL
                JOIN memberships researcher_membership
                  ON researcher_membership.account_id = a.id
                 AND researcher_membership.role = 'researcher'
                 AND researcher_membership.membership_status = 'active'
                JOIN researcher_profiles p ON p.account_id = a.id
                JOIN doctorate_preferences d ON d.researcher_profile_id = p.id
                WHERE lower(l.university_name) = lower(?)
                ORDER BY f.created_at DESC, l.id, a.display_name
                """, (row, rowNum) -> new UniversityResearcherInterest(
                row.getLong("laboratory_id"), row.getString("laboratory_name"),
                row.getString("display_name"), row.getString("current_position"),
                row.getString("career_summary"), row.getString("research_summary"),
                row.getString("preferred_degree_field"), row.getString("preferred_start_term"),
                row.getString("preferred_study_mode"), row.getString("preferred_region"),
                row.getString("work_balance_conditions"),
                row.getObject("favorited_at", java.time.OffsetDateTime.class)), university);
    }

    @Transactional(readOnly = true)
    public List<UniversityNotification> findNotifications(String email) {
        String university = universityName(email);
        return jdbc.query("""
                SELECT n.id, a.display_name, n.laboratory_id, n.laboratory_name,
                       n.created_at, n.read_at
                FROM university_notifications n
                JOIN organizations o ON o.id = n.organization_id
                JOIN accounts a ON a.id = n.researcher_account_id
                WHERE lower(o.name) = lower(?)
                ORDER BY n.created_at DESC, n.id DESC
                LIMIT 100
                """, (row, rowNum) -> new UniversityNotification(
                row.getLong("id"), row.getString("display_name"), row.getLong("laboratory_id"),
                row.getString("laboratory_name"),
                row.getObject("created_at", java.time.OffsetDateTime.class),
                row.getObject("read_at", java.time.OffsetDateTime.class) != null), university);
    }

    @Transactional
    public void markNotificationsRead(String email) {
        String university = universityName(email);
        jdbc.update("""
                UPDATE university_notifications n SET read_at = CURRENT_TIMESTAMP
                FROM organizations o
                WHERE o.id = n.organization_id AND lower(o.name) = lower(?) AND n.read_at IS NULL
                """, university);
    }

    private String universityName(String email) {
        try {
            return jdbc.queryForObject("""
                    SELECT o.name
                    FROM accounts a
                    JOIN memberships m ON m.account_id = a.id
                    JOIN organizations o ON o.id = m.organization_id
                    WHERE lower(a.email) = lower(?) AND a.account_status = 'active'
                      AND m.role = 'university_staff' AND m.membership_status = 'active'
                      AND o.organization_type = 'university'
                    LIMIT 1
                    """, String.class, email);
        } catch (EmptyResultDataAccessException exception) {
            throw new AccessDeniedException("有効な大学担当者アカウントがありません。");
        }
    }

    private LaboratoryRequest withUniversity(LaboratoryRequest request, String university) {
        return new LaboratoryRequest(university, request.graduateSchoolName(), request.laboratoryName(),
                request.supervisorName(), request.researchField(), request.researchSummary(),
                request.acceptsWorkingProfessionals());
    }
}
