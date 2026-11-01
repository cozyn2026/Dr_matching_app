package jp.drmatching.researcher;

import java.util.List;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/favorites")
public class ResearcherFavoriteController {
    private final JdbcTemplate jdbc;

    public ResearcherFavoriteController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping
    public List<Long> findMine(Authentication authentication) {
        return jdbc.queryForList("""
                SELECT f.laboratory_id
                FROM researcher_favorites f
                JOIN accounts a ON a.id = f.account_id
                WHERE lower(a.email) = lower(?)
                ORDER BY f.created_at DESC, f.laboratory_id
                """, Long.class, authentication.getName());
    }

    @PutMapping("/{laboratoryId}")
    @Transactional
    public ResponseEntity<Void> add(
            @PathVariable Long laboratoryId, Authentication authentication) {
        Integer exists = jdbc.queryForObject(
                "SELECT COUNT(*) FROM laboratories WHERE id = ?", Integer.class, laboratoryId);
        if (exists == null || exists == 0) return ResponseEntity.notFound().build();

        int inserted = jdbc.update("""
                INSERT INTO researcher_favorites (account_id, laboratory_id)
                SELECT id, ? FROM accounts WHERE lower(email) = lower(?) AND account_status = 'active'
                ON CONFLICT (account_id, laboratory_id) DO NOTHING
                """, laboratoryId, authentication.getName());
        if (inserted > 0) {
            jdbc.update("""
                    INSERT INTO university_notifications
                        (organization_id, researcher_account_id, laboratory_id, laboratory_name)
                    SELECT o.id, a.id, l.id, l.laboratory_name
                    FROM laboratories l
                    JOIN organizations o ON o.organization_type = 'university'
                                         AND lower(o.name) = lower(l.university_name)
                    JOIN accounts a ON lower(a.email) = lower(?)
                    WHERE l.id = ?
                    """, authentication.getName(), laboratoryId);
        }
        String scope = "laboratory:" + laboratoryId;
        jdbc.update("""
                INSERT INTO consent_records (account_id, consent_type, policy_version, scope)
                SELECT id, 'favorite_profile_sharing', 'development-v1', ?
                FROM accounts a
                WHERE lower(a.email) = lower(?)
                  AND NOT EXISTS (
                      SELECT 1 FROM consent_records c
                      WHERE c.account_id = a.id AND c.consent_type = 'favorite_profile_sharing'
                        AND c.scope = ? AND c.revoked_at IS NULL
                  )
                """, scope, authentication.getName(), scope);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{laboratoryId}")
    @Transactional
    public ResponseEntity<Void> remove(
            @PathVariable Long laboratoryId, Authentication authentication) {
        jdbc.update("""
                DELETE FROM researcher_favorites f
                USING accounts a
                WHERE f.account_id = a.id AND lower(a.email) = lower(?) AND f.laboratory_id = ?
                """, authentication.getName(), laboratoryId);
        jdbc.update("""
                UPDATE consent_records c SET revoked_at = CURRENT_TIMESTAMP
                FROM accounts a
                WHERE c.account_id = a.id AND lower(a.email) = lower(?)
                  AND c.consent_type = 'favorite_profile_sharing'
                  AND c.scope = ? AND c.revoked_at IS NULL
                """, authentication.getName(), "laboratory:" + laboratoryId);
        return ResponseEntity.noContent().build();
    }
}
