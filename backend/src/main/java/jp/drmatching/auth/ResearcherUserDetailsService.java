package jp.drmatching.auth;

import java.util.Locale;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class ResearcherUserDetailsService implements UserDetailsService {
    private final JdbcTemplate jdbc;

    private record ActiveMembership(String passwordHash, String role) {}

    public ResearcherUserDetailsService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        try {
            List<ActiveMembership> memberships = jdbc.query("""
                    SELECT a.password_hash, m.role
                    FROM accounts a
                    JOIN memberships m ON m.account_id = a.id
                    WHERE lower(a.email) = lower(?)
                      AND a.account_status = 'active'
                      AND m.membership_status = 'active'
                      AND a.password_hash IS NOT NULL
                      AND m.role IN ('researcher', 'university_staff')
                    """, (result, rowNum) -> new ActiveMembership(
                    result.getString("password_hash"), result.getString("role")), email.strip().toLowerCase(Locale.ROOT));
            if (memberships.isEmpty()) throw new UsernameNotFoundException("Invalid credentials");
            return User.withUsername(email.strip().toLowerCase(Locale.ROOT))
                    .password(memberships.getFirst().passwordHash())
                    .authorities(memberships.stream().map(ActiveMembership::role).distinct()
                            .map(role -> "ROLE_" + role.toUpperCase(Locale.ROOT)).toArray(String[]::new))
                    .build();
        } catch (org.springframework.dao.EmptyResultDataAccessException exception) {
            throw new UsernameNotFoundException("Invalid credentials");
        }
    }
}
