package me.bannock.website.security;

import me.bannock.website.security.authentication.AuthFailHandlerImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfiguration {

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public AuthenticationFailureHandler authenticationFailureHandler(){
        return new AuthFailHandlerImpl();
    }

    @Bean
    @Autowired
    public DefaultSecurityFilterChain configureHttp(HttpSecurity security,
                                                    AuthenticationFailureHandler authFailureHandler) throws Exception {
        security.sessionManagement(sessionManagement -> {
            sessionManagement.sessionCreationPolicy(SessionCreationPolicy.ALWAYS);
        });

        security.authorizeHttpRequests(authManagerRegistry -> authManagerRegistry.requestMatchers(
                "/", "/core/", "/core/login*", "/core/register*",
                "/error*", "/resources/**", "/blog/**", "/about/**"
        ).permitAll().anyRequest().authenticated());

        security.anonymous(anonymousConfigurer -> {
            anonymousConfigurer.authorities(Roles.DEFAULT_ANON_ROLES).configure(security);
        });

        // Configure login
        security.formLogin(loginConfigurer -> loginConfigurer.loginProcessingUrl("/core/processLogin")
                .defaultSuccessUrl("/?loggedIn=true", true)
                .usernameParameter("email")
                .passwordParameter("password")
                .loginPage("/core/login")
                .failureHandler(authFailureHandler)
                .permitAll()
        );

        security.logout(logoutConfigurer -> logoutConfigurer
                .logoutUrl("/core/logout")
                .logoutSuccessUrl("/")
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
        );

        security.csrf(Customizer.withDefaults());

        return security.build();
    }

    @Bean
    public GrantedAuthorityDefaults getGrantedAuthorityDefaults(){
        return new GrantedAuthorityDefaults("");
    }

}
