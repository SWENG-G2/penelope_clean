package sweng.penelope.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private static final AntPathRequestMatcher REQUEST_MATCHER = new AntPathRequestMatcher("/api/**");

    @Value("${penelope.api-principalHeader}")
    private String principalHeader;

    @Value("${penelope.api-credentialsHeader}")
    private String credentialsHeader;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity,
            ApiKeyAuthenticationManager apiKeyAuthenticationManager) throws Exception {
        // Instantiate filter
        ApiKeyFilter apiKeyFilter = new ApiKeyFilter(apiKeyAuthenticationManager, principalHeader, credentialsHeader);

        // Add filter to chain
        httpSecurity.antMatcher(REQUEST_MATCHER.getPattern()).csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .addFilter(apiKeyFilter)
                .authorizeRequests()
                .anyRequest()
                .authenticated(); // Only allow auth'd requests for supplied pattern.

        return httpSecurity.build();
    }
}
