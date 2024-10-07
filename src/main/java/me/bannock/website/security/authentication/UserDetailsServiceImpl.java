package me.bannock.website.security.authentication;

import me.bannock.website.services.user.UserService;
import me.bannock.website.services.user.UserServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    public UserDetailsServiceImpl(UserService userService){
        this.userService = userService;
    }

    private final UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            return new UserDetailsImpl(userService.getUserWithEmail(username));
        } catch (UserServiceException e) {
            throw new UsernameNotFoundException("Could not find user with provided name: %s".formatted(e.getUserFriendlyError()), e);
        }
    }

}
