package jp.drmatching.admin;

import java.util.List;
import java.util.Map;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ResearcherRegistrationAdminService {
    private final JdbcTemplate jdbc;

    public ResearcherRegistrationAdminService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional(readOnly = true)
    public List<PendingResearcherRegistration> findForReview() {
        return jdbc.query("""
                SELECT m.id AS membership_id, m.membership_status, a.display_name, a.email,
                       o.name AS organization_name,
                       p.current_position, p.career_summary, p.research_summary,
                       d.preferred_degree_field, d.preferred_start_term, d.preferred_study_mode,
                       d.preferred_region, d.work_balance_conditions, m.created_at AS submitted_at
                FROM memberships m
                JOIN accounts a ON a.id = m.account_id
                JOIN organizations o ON o.id = m.organization_id
                JOIN researcher_profiles p ON p.account_id = a.id
                JOIN doctorate_preferences d ON d.researcher_profile_id = p.id
                WHERE m.role = 'researcher'
                  AND m.membership_status IN ('pending', 'rejected', 'active')
                ORDER BY CASE m.membership_status WHEN 'pending' THEN 0 WHEN 'rejected' THEN 1 ELSE 2 END,
                         m.created_at, m.id
                """, (result, rowNum) -> new PendingResearcherRegistration(
                result.getLong("membership_id"), result.getString("membership_status"),
                result.getString("display_name"),
                result.getString("email"), result.getString("organization_name"),
                result.getString("current_position"), result.getString("career_summary"),
                result.getString("research_summary"), result.getString("preferred_degree_field"),
                result.getString("preferred_start_term"), result.getString("preferred_study_mode"),
                result.getString("preferred_region"), result.getString("work_balance_conditions"),
                result.getObject("submitted_at", java.time.OffsetDateTime.class)));
    }

    @Transactional
    public RegistrationReviewResponse decide(Long membershipId, String decision) {
        Map<String, Object> registration;
        try {
            registration = jdbc.queryForMap("""
                    SELECT account_id, organization_id
                    FROM memberships
                    WHERE id = ? AND role = 'researcher' AND membership_status = 'pending'
                    FOR UPDATE
                    """, membershipId);
        } catch (EmptyResultDataAccessException exception) {
            throw new RegistrationReviewNotFoundException(membershipId);
        }
        Long accountId = ((Number) registration.get("account_id")).longValue();
        Long organizationId = ((Number) registration.get("organization_id")).longValue();
        Long administratorId = jdbc.queryForObject(
                "SELECT id FROM administrators WHERE active = TRUE ORDER BY id LIMIT 1", Long.class);

        boolean approved = decision.equals("approved");
        String membershipStatus = approved ? "active" : "rejected";
        jdbc.update("""
                UPDATE memberships
                SET membership_status = ?, approved_at = CASE WHEN ? THEN CURRENT_TIMESTAMP ELSE NULL END
                WHERE id = ?
                """, membershipStatus, approved, membershipId);
        jdbc.update("UPDATE accounts SET account_status = ? WHERE id = ?",
                approved ? "active" : "suspended", accountId);
        if (approved) {
            jdbc.update("""
                    UPDATE organizations
                    SET membership_status = 'active', approved_at = CURRENT_TIMESTAMP,
                        updated_at = CURRENT_TIMESTAMP
                    WHERE id = ?
                    """, organizationId);
        }
        jdbc.update("""
                INSERT INTO membership_reviews (membership_id, administrator_id, decision)
                VALUES (?, ?, ?)
                """, membershipId, administratorId, decision);

        return new RegistrationReviewResponse(decision,
                approved ? "研究者登録を承認しました。" : "研究者登録を却下しました。");
    }
}
