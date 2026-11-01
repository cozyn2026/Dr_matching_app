package jp.drmatching.researcher;

import java.util.Locale;
import java.nio.charset.StandardCharsets;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ResearcherRegistrationService {
    private final JdbcTemplate jdbc;
    private final PasswordEncoder passwordEncoder;

    public ResearcherRegistrationService(JdbcTemplate jdbc, PasswordEncoder passwordEncoder) {
        this.jdbc = jdbc;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public ResearcherRegistrationResponse register(ResearcherRegistrationRequest request) {
        String email = request.email().strip().toLowerCase(Locale.ROOT);
        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new InvalidResearcherPasswordException();
        }
        try {
            Long organizationId = jdbc.queryForObject("""
                    INSERT INTO organizations (organization_type, name)
                    VALUES ('company', ?)
                    ON CONFLICT (organization_type, name) DO UPDATE SET name = EXCLUDED.name
                    RETURNING id
                    """, Long.class, request.organizationName().strip());

            Long accountId = jdbc.queryForObject("""
                    INSERT INTO accounts (email, password_hash, display_name)
                    VALUES (?, ?, ?)
                    RETURNING id
                    """, Long.class, email, passwordEncoder.encode(request.password()), request.displayName().strip());

            jdbc.update("""
                    INSERT INTO memberships (account_id, organization_id, role)
                    VALUES (?, ?, 'researcher')
                    """, accountId, organizationId);

            Long profileId = jdbc.queryForObject("""
                    INSERT INTO researcher_profiles (account_id, current_position, career_summary, research_summary)
                    VALUES (?, ?, ?, ?)
                    RETURNING id
                    """, Long.class, accountId, request.currentPosition().strip(),
                    request.careerSummary().strip(), request.researchSummary().strip());

            jdbc.update("""
                    INSERT INTO doctorate_preferences (
                        researcher_profile_id, preferred_degree_field, preferred_start_term,
                        preferred_study_mode, preferred_region, work_balance_conditions
                    ) VALUES (?, ?, ?, ?, ?, ?)
                    """, profileId, request.preferredDegreeField().strip(), request.preferredStartTerm().strip(),
                    request.preferredStudyMode().strip(), request.preferredRegion().strip(),
                    request.workBalanceConditions().strip());

            jdbc.update("""
                    INSERT INTO consent_records (account_id, consent_type, policy_version, scope)
                    VALUES (?, 'researcher_registration', 'development-v1', '会員申請の確認に必要な範囲')
                    """, accountId);
        } catch (DuplicateKeyException exception) {
            throw new ResearcherRegistrationConflictException();
        }

        return new ResearcherRegistrationResponse(
                "pending", "研究者登録を受け付けました。会員確認後に利用を開始できます。");
    }
}
