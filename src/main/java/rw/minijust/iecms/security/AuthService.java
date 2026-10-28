package rw.minijust.iecms.security;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.minijust.iecms.audit.AuditService;
import rw.minijust.iecms.security.AuthDtos.*;

@Service
public class AuthService {
    static final int MAX_FAILED_ATTEMPTS = 5;
    static final Duration LOCK_DURATION = Duration.ofMinutes(15);
    static final Duration TOKEN_TTL = Duration.ofHours(1);
    private static final String INVALID = "Invalid username or password";

    private final AppUserRepository users;
    private final PasswordEncoder encoder;
    private final JwtEncoder jwt;
    private final AuditService audit;
    private final String dummyHash;

    public AuthService(AppUserRepository users, PasswordEncoder encoder, JwtEncoder jwt, AuditService audit) {
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
        this.audit = audit;
        this.dummyHash = encoder.encode("timing-equaliser");
    }

    // Failed attempts must be saved even though we throw, so don't roll back on auth errors
    @Transactional(noRollbackFor = AuthenticationException.class)
    public TokenResponse login(LoginRequest req) {
        AppUser user = users.findByUsername(req.username()).orElse(null);
        if (user == null) {
            encoder.matches(req.password(), dummyHash); // same response time whether or not the user exists
            audit.record(req.username(), "LOGIN_FAILED", "AppUser", req.username(), "unknown user");
            throw new BadCredentialsException(INVALID); // same message: don't reveal which usernames exist
        }
        Instant now = Instant.now();
        if (user.isLocked(now)) {
            audit.record(user.getUsername(), "LOGIN_BLOCKED", "AppUser", user.getUsername(), "account locked");
            throw new LockedException("Account locked after too many failed attempts. Try again later.");
        }
        if (!user.isEnabled()) {
            throw new DisabledException("Account disabled");
        }
        if (!encoder.matches(req.password(), user.getPasswordHash())) {
            user.registerFailedLogin(now, MAX_FAILED_ATTEMPTS, LOCK_DURATION);
            audit.record(user.getUsername(), "LOGIN_FAILED", "AppUser", user.getUsername(), "wrong password");
            throw new BadCredentialsException(INVALID);
        }
        user.registerSuccessfulLogin();
        audit.record(user.getUsername(), "LOGIN_SUCCESS", "AppUser", user.getUsername(), null);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("iecms-mini")
                .subject(user.getUsername())
                .issuedAt(now)
                .expiresAt(now.plus(TOKEN_TTL))
                .claim("roles", List.of(user.getRole().name()))
                .build();
        String token = jwt.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();
        return new TokenResponse(token, "Bearer", TOKEN_TTL.toSeconds(), user.getUsername(), user.getRole());
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest req) {
        if (users.existsByUsername(req.username())) {
            throw new IllegalStateException("Username " + req.username() + " is already taken");
        }
        AppUser user = users.save(new AppUser(req.username(), encoder.encode(req.password()), req.role()));
        audit.record("USER_CREATED", "AppUser", user.getUsername(), "role=" + user.getRole());
        return UserResponse.of(user);
    }
}
