package no.benidorm.qr.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import no.benidorm.qr.common.AuditableEntity;

@Entity
@Table(name = "app_user")
public class AppUser extends AuditableEntity {
    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 320)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false, length = 160)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private UserRole role;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column
    private Instant emailVerifiedAt;

    @Column(length = 128)
    private String emailVerificationTokenHash;

    @Column
    private Instant emailVerificationExpiresAt;

    protected AppUser() {
    }

    public AppUser(String email, String passwordHash, String fullName, UserRole role) {
        this.id = UUID.randomUUID();
        this.email = email.toLowerCase();
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.role = role;
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public UserRole getRole() {
        return role;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Instant getEmailVerifiedAt() {
        return emailVerifiedAt;
    }

    public Instant getEmailVerificationExpiresAt() {
        return emailVerificationExpiresAt;
    }

    public void updateProfile(String email, String fullName) {
        this.email = email.toLowerCase();
        this.fullName = fullName;
    }

    public void setEmailVerified(boolean verified) {
        this.enabled = verified;
        this.emailVerifiedAt = verified ? Instant.now() : null;
        this.emailVerificationTokenHash = null;
        this.emailVerificationExpiresAt = null;
    }

    public void startEmailVerification(String tokenHash, Instant expiresAt) {
        this.enabled = false;
        this.emailVerifiedAt = null;
        this.emailVerificationTokenHash = tokenHash;
        this.emailVerificationExpiresAt = expiresAt;
    }

    public void verifyEmail() {
        this.enabled = true;
        this.emailVerifiedAt = Instant.now();
        this.emailVerificationTokenHash = null;
        this.emailVerificationExpiresAt = null;
    }
}
