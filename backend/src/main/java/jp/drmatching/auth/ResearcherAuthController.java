package jp.drmatching.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class ResearcherAuthController {
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final CookieCsrfTokenRepository csrfTokenRepository;
    private final JdbcTemplate jdbc;

    public ResearcherAuthController(
            AuthenticationManager authenticationManager,
            SecurityContextRepository securityContextRepository,
            CookieCsrfTokenRepository csrfTokenRepository,
            JdbcTemplate jdbc) {
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
        this.csrfTokenRepository = csrfTokenRepository;
        this.jdbc = jdbc;
    }

    @GetMapping("/csrf")
    public CsrfTokenResponse csrf(CsrfToken csrfToken) {
        return new CsrfTokenResponse(csrfToken.getToken());
    }

    @PostMapping("/login")
    public ResponseEntity<ResearcherSessionResponse> login(
            @Valid @RequestBody ResearcherLoginRequest request,
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(
                            request.email().strip().toLowerCase(Locale.ROOT), request.password()));
        } catch (AuthenticationException exception) {
            throw new InvalidCredentialsException();
        }

        servletRequest.getSession(true);
        servletRequest.changeSessionId();
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, servletRequest, servletResponse);
        csrfTokenRepository.saveToken(null, servletRequest, servletResponse);
        jdbc.update("UPDATE accounts SET last_login_at = CURRENT_TIMESTAMP WHERE lower(email) = lower(?)",
                authentication.getName());
        return ResponseEntity.ok(sessionResponse(authentication.getName()));
    }

    @GetMapping("/me")
    public ResearcherSessionResponse me(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new InvalidCredentialsException();
        }
        return sessionResponse(authentication.getName());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        SecurityContextHolder.clearContext();
        var session = request.getSession(false);
        if (session != null) session.invalidate();
        csrfTokenRepository.saveToken(null, request, response);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    private ResearcherSessionResponse sessionResponse(String email) {
        return jdbc.queryForObject("""
                SELECT a.email, a.display_name, m.role, o.name AS organization_name
                FROM accounts a
                JOIN memberships m ON m.account_id = a.id AND m.membership_status = 'active'
                JOIN organizations o ON o.id = m.organization_id
                WHERE lower(a.email) = lower(?) AND a.account_status = 'active'
                  AND m.role IN ('researcher', 'university_staff')
                ORDER BY CASE m.role WHEN 'researcher' THEN 0 ELSE 1 END
                LIMIT 1
                """, (result, rowNum) -> new ResearcherSessionResponse(
                result.getString("email"), result.getString("display_name"), result.getString("role"),
                result.getString("organization_name")), email);
    }
}
