package projekt.PD.Security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.HiddenHttpMethodFilter;
import projekt.PD.Security.PageExceptions.CustomAuthenticationFailureHandler;

/*
 * Klasa SecurityConfig jest odpowiedzialna za konfigurację bezpieczeństwa aplikacji.
 */

@EnableWebSecurity
@EnableMethodSecurity(
        securedEnabled = true,
        jsr250Enabled = true
)
@Configuration
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Bean
    public FilterRegistrationBean<HiddenHttpMethodFilter> hiddenHttpMethodFilter() {
        FilterRegistrationBean<HiddenHttpMethodFilter> filterRegistrationBean = new FilterRegistrationBean<>(new HiddenHttpMethodFilter());
        filterRegistrationBean.addUrlPatterns("/*");
        return filterRegistrationBean;
    }

/*
 * SecurityFilterChain jest odpowiedzialny za konfigurację filtrów bezpieczeństwa w aplikacji.
 * Definiowane są tu reguły dotyczące autoryzacji, logowania, wylogowywania i zarządzania sesjami.
 * 
 */


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.csrf(csrf->csrf
                .disable()
        );

        http.authorizeHttpRequests(authorize->authorize
                .requestMatchers("/css/**","/api/**","/login","/register","/")
                .permitAll());

        http.authorizeHttpRequests(authorize->authorize
                .anyRequest()
                .authenticated()
        );

        http.formLogin((form) -> form
                .loginPage("/login")
                .failureHandler(authenticationFailureHandler())
                .loginProcessingUrl("/performLogin")
                .defaultSuccessUrl("/home",true)
                .usernameParameter("username")
                .passwordParameter("password")
                .permitAll()
        );

        http.logout(logout -> logout
                .logoutUrl("/performLogout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
        );

        http.headers(headers->headers
                .cacheControl(HeadersConfigurer.CacheControlConfig::disable)
        );

        http.sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
        );



        return http.build();
    }

    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        return new CustomAuthenticationFailureHandler();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }


    /*
     * RoleHierarchy jest używany do definiowania hierarchii ról w aplikacji.
     * Dzięki temu, jeśli użytkownik ma rolę nadrzędną, automatycznie zyskuje dostęp do ról podrzędnych.
     */
    @Bean
    public RoleHierarchy roleHierarchy() {
        RoleHierarchyImpl hierarchy = new RoleHierarchyImpl();

        String hierarchyString = """
            ROLE_ADMIN > ROLE_MODERATOR
            ROLE_MODERATOR > ROLE_TRAINER
            ROLE_TRAINER > ROLE_USER
        """;

        hierarchy.setHierarchy(hierarchyString);
        return hierarchy;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }


    /*
     * DaoAuthenticationProvider jest odpowiedzialny za uwierzytelnianie użytkowników na podstawie danych przechowywanych w bazie danych.
     * Używa UserDetailsService do pobierania informacji o użytkownikach i PasswordEncoder do porównywania haseł.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
}
