package rw.minijust.iecms.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Creates one user per role on an empty database, for demos and local testing only. */
@Component
@ConditionalOnProperty(name = "app.demo-users.enabled", havingValue = "true")
public class DemoUserSeeder implements ApplicationRunner {
    public static final String DEMO_PASSWORD = "Demo@12345";
    private static final Logger log = LoggerFactory.getLogger(DemoUserSeeder.class);

    private final AppUserRepository users;
    private final PasswordEncoder encoder;

    public DemoUserSeeder(AppUserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (users.count() > 0) return;
        for (Role role : Role.values()) {
            users.save(new AppUser(role.name().toLowerCase(), encoder.encode(DEMO_PASSWORD), role));
        }
        log.warn("Demo users created (admin, registrar, judge, viewer). Set APP_DEMO_USERS_ENABLED=false in production.");
    }
}
