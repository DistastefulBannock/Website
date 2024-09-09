package me.bannock.website.services.user.impl.hibernate;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import org.springframework.lang.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Entity
@Table(name = "core_users", indexes = {
        @Index(columnList = "id", unique = true),
        @Index(columnList = "name", unique = true),
        @Index(columnList = "email", unique = true)
})
public class UserEntity implements Serializable {

    public UserEntity(){}

    public UserEntity(String name, String email, String lastIp){
        this(name, email, lastIp, null, new ArrayList<>());
    }

    public UserEntity(String name, String email, String lastIp, @Nullable String password, List<String> roles) {
        this(
                name, email,
                lastIp, password,
                roles, false,
                false, false,
                password == null
        );
    }

    public UserEntity(String name, String email,
                      String lastIp, @Nullable String password,
                      List<String> roles, boolean verified,
                      boolean disabled, boolean shadowBanned,
                      boolean unclaimed) {
        Objects.requireNonNull(name);
        if (password == null && unclaimed)
            throw new IllegalArgumentException("Claimed accounts must have a set password");
        Objects.requireNonNull(email);
        Objects.requireNonNull(lastIp);
        Objects.requireNonNull(roles);

        this.name = name;
        this.email = email;
        this.lastIp = lastIp;
        this.password = password;
        this.roles = roles;
        this.verified = verified;
        this.disabled = disabled;
        this.shadowBanned = shadowBanned;
        this.unclaimed = unclaimed;
    }

    @Id
    @Column(name = "id", unique = true, nullable = false)
    @SequenceGenerator(name = "core_uid_seq", sequenceName = "core_uid_seq", initialValue = 0, allocationSize = 1)
    @GeneratedValue(generator = "core_uid_seq")
    private Long id;

    @Column(name = "name", unique = true, nullable = false)
    private String name;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "lastIp", nullable = false)
    private String lastIp;

    @Column(name = "password")
    private @Nullable String password;

    @Column(name = "roles")
    @ElementCollection(targetClass = String.class, fetch = FetchType.LAZY)
    @CollectionTable(name = "core_user_roles", joinColumns = @JoinColumn(name = "id"), indexes = {
            @Index(columnList = "id"),
            @Index(columnList = "roles")
    })
    private List<String> roles;

    @Column(name = "verified", nullable = false)
    private boolean verified;

    @Column(name = "disabled", nullable = false)
    private boolean disabled;

    @Column(name = "shadowBanned", nullable = false)
    private boolean shadowBanned;

    @Column(name = "unclaimed", nullable = false)
    private boolean unclaimed;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLastIp() {
        return lastIp;
    }

    public void setLastIp(String lastIp) {
        this.lastIp = lastIp;
    }

    public Optional<String> getPassword() {
        return Optional.ofNullable(password);
    }

    public void setPassword(@Nullable String password) {
        this.password = password;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        Objects.requireNonNull(roles);
        this.roles = roles;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean emailVerified) {
        this.verified = emailVerified;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean accountDisabled) {
        this.disabled = accountDisabled;
    }

    public boolean isShadowBanned() {
        return shadowBanned;
    }

    public void setShadowBanned(boolean shadowBanned) {
        this.shadowBanned = shadowBanned;
    }

    public boolean isUnclaimed() {
        return unclaimed;
    }

    public void setUnclaimed(boolean unclaimedAccount) {
        this.unclaimed = unclaimedAccount;
    }

    @Override
    public String toString() {
        return "UserEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", lastIp='" + lastIp + '\'' +
                ", emailVerified=" + verified +
                ", accountDisabled=" + disabled +
                ", shadowBanned=" + shadowBanned +
                ", unclaimedAccount=" + unclaimed +
                '}';
    }

}
