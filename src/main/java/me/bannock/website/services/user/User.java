package me.bannock.website.services.user;

import org.springframework.lang.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class User {

    /**
     * @param id The id of the user
     * @param name The user's name
     * @param email The user's email
     * @param lastIp The user's last ip address
     * @param password The user's password
     * @param roles The roles that the user has
     * @param emailVerified Whether the user's email is verified
     * @param accountDisabled Whether the user is disabled
     * @param shadowBanned Whether the user is shadow-banned
     * @param unclaimedAccount Whether the user has claimed their account,
     *                         meaning that it must be logged into in order to be used
     */
    public User(long id, String name,
                String email, String lastIp,
                @Nullable String password, List<String> roles,
                boolean emailVerified, boolean accountDisabled,
                boolean shadowBanned, boolean unclaimedAccount) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.lastIp = lastIp;
        this.password = password;
        this.roles = roles;
        this.emailVerified = emailVerified;
        this.accountDisabled = accountDisabled;
        this.shadowBanned = shadowBanned;
        this.unclaimedAccount = unclaimedAccount;
    }

    private final long id;
    private final String name, email, lastIp;
    /**
     * Unclaimed users have no password
     */
    private final @Nullable String password;
    private final List<String> roles;
    private final boolean emailVerified, accountDisabled, shadowBanned, unclaimedAccount;

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getLastIp() {
        return lastIp;
    }

    public Optional<String> getPassword() {
        return Optional.ofNullable(password);
    }

    public List<String> getRoles() {
        return Collections.unmodifiableList(roles);
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public boolean isAccountDisabled() {
        return accountDisabled;
    }

    public boolean isShadowBanned() {
        return shadowBanned;
    }

    public boolean isUnclaimedAccount() {
        return unclaimedAccount;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", lastIp='" + lastIp + '\'' +
                ", emailVerified=" + emailVerified +
                ", accountDisabled=" + accountDisabled +
                ", shadowBanned=" + shadowBanned +
                ", unclaimedAccount=" + unclaimedAccount +
                ", roles=" + roles +
                '}';
    }

}
