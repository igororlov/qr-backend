package no.benidorm.qr.company;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import no.benidorm.qr.auth.AppUser;
import no.benidorm.qr.common.AuditableEntity;

@Entity
@Table(name = "company")
public class Company extends AuditableEntity {
    @Id
    private UUID id;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(nullable = false, unique = true, length = 120)
    private String slug;

    @Column(columnDefinition = "text")
    private String logoUrl;

    @Column(length = 120)
    private String logoContentType;

    @Column(columnDefinition = "bytea")
    private byte[] logoBytes;

    private Instant logoUploadedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_user_id")
    private AppUser owner;

    @Column(nullable = false)
    private boolean active = true;

    protected Company() {
    }

    public Company(String name, String slug, String logoUrl, AppUser owner) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.slug = slug;
        this.logoUrl = logoUrl;
        this.owner = owner;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public String getLogoContentType() {
        return logoContentType;
    }

    public byte[] getLogoBytes() {
        return logoBytes;
    }

    public Instant getLogoUploadedAt() {
        return logoUploadedAt;
    }

    public AppUser getOwner() {
        return owner;
    }

    public boolean isActive() {
        return active;
    }

    public void update(String name, String slug, String logoUrl, boolean active) {
        this.name = name;
        this.slug = slug;
        this.logoUrl = logoUrl;
        this.active = active;
    }

    public void changeOwner(AppUser owner) {
        this.owner = owner;
    }

    public void storeLogo(String logoUrl, String contentType, byte[] bytes) {
        this.logoUrl = logoUrl;
        this.logoContentType = contentType;
        this.logoBytes = bytes;
        this.logoUploadedAt = Instant.now();
    }
}
