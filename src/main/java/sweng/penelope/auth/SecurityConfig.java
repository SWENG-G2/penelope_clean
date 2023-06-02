package sweng.penelope.auth;

import java.security.KeyPair;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private static final AntPathRequestMatcher REQUEST_MATCHER = new AntPathRequestMatcher("/api/**");

    @Value("${penelope.api-credentialsHeader}")
    private String credentialsHeader;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity,
            UserAuthenticationManager userAuthenticationManager, KeyPair serverKeyPair) throws Exception {
        // Instantiate filters
        UserFilter userFilter = new UserFilter(userAuthenticationManager, serverKeyPair, credentialsHeader);
        ExceptionFilter exceptionFilter = new ExceptionFilter();

        httpSecurity.csrf().disable().requiresChannel(channel -> channel.anyRequest().requiresSecure())
                .authorizeHttpRequests((authorizeHttpRequests) -> authorizeHttpRequests
                        .antMatchers(REQUEST_MATCHER.getPattern()).authenticated())
                .addFilterBefore(exceptionFilter, CorsFilter.class)
                .addFilter(userFilter)
                .sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return httpSecurity.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().antMatchers("/api/users/validate");
    }
}
