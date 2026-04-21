package no.benidorm.qr.qrcode;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import no.benidorm.qr.common.AuditableEntity;
import no.benidorm.qr.company.Company;

@Entity
@Table(name = "qr_code")
public class QrCode extends AuditableEntity {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id")
    private Company company;

    @Column(nullable = false, unique = true, length = 120)
    private String slug;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(length = 240)
    private String subtitle;

    @Column(length = 120)
    private String label;

    @Column(columnDefinition = "text")
    private String logoUrl;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private long scanCount;

    @Column(nullable = false, length = 7)
    private String buttonColor = "#187466";

    @Column(nullable = false, length = 7)
    private String qrForegroundColor = "#111111";

    @Column(nullable = false, length = 7)
    private String qrBackgroundColor = "#ffffff";

    @Column(nullable = false)
    private boolean qrLogoEnabled = true;

    @Column(name = "qr_image_png", columnDefinition = "bytea")
    private byte[] qrImagePng;

    @Column(name = "qr_image_svg", columnDefinition = "text")
    private String qrImageSvg;

    private Instant qrImageGeneratedAt;

    @OneToMany(mappedBy = "qrCode", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position asc")
    private List<QrAction> actions = new ArrayList<>();

    protected QrCode() {
    }

    public QrCode(Company company, String slug, String title, String subtitle, String label, String logoUrl) {
        this.id = UUID.randomUUID();
        this.company = company;
        this.slug = slug;
        this.title = title;
        this.subtitle = subtitle;
        this.label = label;
        this.logoUrl = logoUrl;
    }

    public UUID getId() {
        return id;
    }

    public Company getCompany() {
        return company;
    }

    public String getSlug() {
        return slug;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getLabel() {
        return label;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public boolean isActive() {
        return active;
    }

    public long getScanCount() {
        return scanCount;
    }

    public String getQrForegroundColor() {
        return qrForegroundColor;
    }

    public String getButtonColor() {
        return buttonColor;
    }

    public String getQrBackgroundColor() {
        return qrBackgroundColor;
    }

    public boolean isQrLogoEnabled() {
        return qrLogoEnabled;
    }

    public byte[] getQrImagePng() {
        return qrImagePng;
    }

    public Instant getQrImageGeneratedAt() {
        return qrImageGeneratedAt;
    }

    public String getQrImageSvg() {
        return qrImageSvg;
    }

    public List<QrAction> getActions() {
        return actions;
    }

    public void update(String slug, String title, String subtitle, String label, String logoUrl, boolean active) {
        this.slug = slug;
        this.title = title;
        this.subtitle = subtitle;
        this.label = label;
        this.logoUrl = logoUrl;
        this.active = active;
    }

    public void updateButtonColor(String buttonColor) {
        this.buttonColor = buttonColor;
    }

    public void updateImageStyle(String foregroundColor, String backgroundColor, boolean logoEnabled) {
        this.qrForegroundColor = foregroundColor;
        this.qrBackgroundColor = backgroundColor;
        this.qrLogoEnabled = logoEnabled;
        this.qrImagePng = null;
        this.qrImageSvg = null;
        this.qrImageGeneratedAt = null;
    }

    public void storeQrImage(byte[] png, String svg) {
        this.qrImagePng = png;
        this.qrImageSvg = svg;
        this.qrImageGeneratedAt = Instant.now();
    }

    public void replaceActions(List<QrAction> newActions) {
        appendActions(newActions);
    }

    public void clearActions() {
        actions.clear();
    }

    public void appendActions(List<QrAction> newActions) {
        newActions.forEach(action -> action.attachTo(this));
        actions.addAll(newActions);
    }

    public void incrementScanCount() {
        scanCount++;
    }
}
