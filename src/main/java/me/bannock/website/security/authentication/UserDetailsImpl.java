package me.bannock.website.security.authentication;

import me.bannock.website.services.user.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class UserDetailsImpl implements UserDetails {

    public UserDetailsImpl(User user){
        this.user = user;
        this.roles = user.getRoles().stream().map(SimpleGrantedAuthority::new).toList();
    }

    private final User user;
    private final Collection<? extends GrantedAuthority> roles;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles;
    }

    @Override
    public String getPassword() {
        return user.getPassword().orElse(null);
    }

    @Override
    public String getUsername() {
        return user.getName();
    }

    @Override
    public boolean isEnabled() {
        return !user.isAccountDisabled();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return user.getPassword().isPresent() && !user.isUnclaimedAccount();
    }

    @Override
    public String toString() {
        return user.toString();
    }
}
