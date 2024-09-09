package me.bannock.website.security;

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
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.DefaultSecurityFilterChain;

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
    @Autowired
    public DefaultSecurityFilterChain configureHttp(HttpSecurity security,
                                                    UserDetailsService userDetailsService) throws Exception {
        security.authorizeHttpRequests(authManagerRegistry -> authManagerRegistry.requestMatchers(
                "/", "/core/", "/core/login*", "/core/register*", "/core/logout*", "/error*", "/resources/**"
        ).permitAll().anyRequest().authenticated());

        security.anonymous(anonymousConfigurer -> {
            anonymousConfigurer.authorities(Roles.DEFAULT_ANON_ROLES).configure(security);
        });

        // Configure login
        security.formLogin(loginConfigurer -> loginConfigurer.loginProcessingUrl("/core/processLogin")
                .defaultSuccessUrl("/?loggedIn=true", true)
                .usernameParameter("name")
                .passwordParameter("password")
                .loginPage("/core/login").permitAll()
        );

        security.logout(logoutConfigurer -> logoutConfigurer.logoutUrl("/core/logout").clearAuthentication(true));

        security.csrf(Customizer.withDefaults());

        return security.build();
    }

    @Bean
    public GrantedAuthorityDefaults getGrantedAuthorityDefaults(){
        return new GrantedAuthorityDefaults("");
    }

}
