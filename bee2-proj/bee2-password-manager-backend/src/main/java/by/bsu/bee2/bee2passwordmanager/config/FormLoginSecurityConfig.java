package by.bsu.bee2.bee2passwordmanager.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class FormLoginSecurityConfig {

    @Autowired
    private CustomAuthenticationProvider authProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Bean
    public InMemoryUserDetailsManager userDetailsService() {
        UserDetails user = User.withUsername("user")
            .password(passwordEncoder.encode("user"))
            .roles("USER")
            .build();
        UserDetails admin = User.withUsername("admin")
            .password(passwordEncoder.encode("admin"))
            .roles("ADMIN")
            .build();
        return new InMemoryUserDetailsManager(user, admin);
    }

    @Bean
    public AuthenticationManager authManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
            http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.authenticationProvider(authProvider);
        return authenticationManagerBuilder.build();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, OnAuthSuccessHandler onAuthSuccessHandler) throws Exception {
        http
            .csrf().disable()
            .authorizeHttpRequests(req -> {
                req.requestMatchers("/login/**").permitAll();
                req.requestMatchers("/plugin/**").permitAll();
                req.requestMatchers("/p/rest/**").permitAll();
                req.requestMatchers("/p/login/**").authenticated();
                req.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();
            })
            .oauth2Login()
            .successHandler(onAuthSuccessHandler.onOAuthAuthSuccess())
            .failureHandler(onAuthSuccessHandler.onOAuthAuthFailure())
            .and()
            .formLogin()
            .successHandler(onAuthSuccessHandler.onFormLoginAuthSuccess())
            .failureHandler(onAuthSuccessHandler.onFormLoginAuthFailure());
        return http.build();
    }

}
