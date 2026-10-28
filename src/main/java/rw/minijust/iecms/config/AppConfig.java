package rw.minijust.iecms.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import java.util.Optional;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import rw.minijust.iecms.audit.AuditService;

@Configuration
@EnableAsync
@EnableCaching
@EnableJpaAuditing
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class AppConfig {

    /** Fills @CreatedBy / @LastModifiedBy with the signed-in username. */
    @Bean
    AuditorAware<String> auditorAware() {
        return () -> Optional.of(AuditService.currentUser());
    }

    /** Adds the "Authorize" button to Swagger UI so you can paste a JWT. */
    @Bean
    OpenAPI openApi() {
        return new OpenAPI()
                .info(new Info().title("IECMS mini API").version("v1"))
                .addSecurityItem(new SecurityRequirement().addList("bearer"))
                .components(new Components().addSecuritySchemes("bearer",
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")));
    }
}
