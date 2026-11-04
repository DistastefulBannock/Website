package me.bannock.website.security.authentication;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AuthenticationProviderImpl implements AuthenticationProvider {

    @Autowired
    public AuthenticationProviderImpl(PasswordEncoder passwordEncoder, UserDetailsService userDetailsService){
        this.passwordEncoder = passwordEncoder;
        this.userDetailsService = userDetailsService;
        this.dummyPasswordHash = passwordEncoder.encode(UUID.randomUUID().toString());
    }
    private final Logger logger = LogManager.getLogger();
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;
    private final String dummyPasswordHash;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        UserDetails userDetails;
        try {
            userDetails = userDetailsService.loadUserByUsername(authentication.getName());
        } catch (org.springframework.security.core.userdetails.UsernameNotFoundException e) {
            runDummyPasswordCheck(authentication);
            throw e;
        }
        
        if (userDetails.getPassword() == null){
            // Unclaimed accounts have no password, and bcrypt returns instantly on a null hash
            runDummyPasswordCheck(authentication);
            logger.info("Failed to get user because their account has no password set, email={}",
                    authentication.getName());
            throw new BadCredentialsException("Account has no password set.");
        }
        if (!passwordEncoder.matches(authentication.getCredentials().toString(), userDetails.getPassword())){
            logger.info("Failed to get user because their provided password doesn't match the account's, email={}",
                    authentication.getName());
            throw new BadCredentialsException("Incorrect password.");
        }
        
        if (!userDetails.isEnabled()){
            logger.warn("Unable to get user because their account is disabled, email={}", authentication.getName());
            throw new DisabledException("Account is disabled");
        }
        if (!userDetails.isCredentialsNonExpired()){
            logger.warn("Unable to get user because their account is not claimed, email={}", authentication.getName());
            throw new BadCredentialsException("Account is unclaimed. Please claim your account to login here, " +
                    "or otherwise simply put your email into any input form to access your unsecured account.");
        }
        
        logger.info("User has logged in, user={}", userDetails);
        return new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(), userDetails.getAuthorities());
    }

    /**
     * Runs a dummy password check with encoder to prevent timing attacks
     * @param authentication The authentication attempt containing the raw password
     */
    private void runDummyPasswordCheck(Authentication authentication){
        passwordEncoder.matches(authentication.getCredentials().toString(), dummyPasswordHash);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }

}
