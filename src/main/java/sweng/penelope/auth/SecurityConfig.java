package sweng.penelope.auth;

import java.security.KeyPair;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.ChannelSecurityConfigurer;
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

    @Value("#{new Boolean('${penelope.ssl:false}')}")
    private Boolean ssl;

    /**
     * Decides whether requests should be over ssl or not depending on value set in
     * config.
     * 
     * @param channel The channel configurer to apply settings to.
     * @return The channel configurer with appropriate settings.
     */
    private ChannelSecurityConfigurer<HttpSecurity>.ChannelRequestMatcherRegistry decideSSL(
            ChannelSecurityConfigurer<HttpSecurity>.ChannelRequestMatcherRegistry channel) {
        return Boolean.TRUE.equals(ssl) ? channel.anyRequest().requiresSecure()
                : channel.anyRequest().requiresInsecure();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity,
            UserAuthenticationManager userAuthenticationManager, KeyPair serverKeyPair) throws Exception {
        // Instantiate filters
        UserFilter userFilter = new UserFilter(userAuthenticationManager, serverKeyPair, credentialsHeader);
        ExceptionFilter exceptionFilter = new ExceptionFilter();

        httpSecurity.csrf().disable().requiresChannel(channel -> decideSSL(channel))
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
