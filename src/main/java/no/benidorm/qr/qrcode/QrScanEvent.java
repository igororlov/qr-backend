package no.benidorm.qr.qrcode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "qr_scan_event")
public class QrScanEvent {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "qr_code_id")
    private QrCode qrCode;

    @Column(length = 100)
    private String visitorId;

    @Column(nullable = false)
    private boolean uniqueVisitor;

    @Column(length = 45)
    private String ipAddress;

    @Column(columnDefinition = "text")
    private String userAgent;

    @Column(length = 40)
    private String deviceType;

    @Column(length = 2)
    private String countryCode;

    @Column(length = 120)
    private String countryName;

    @Column(length = 160)
    private String region;

    @Column(length = 160)
    private String city;

    private Double latitude;

    private Double longitude;

    @Column(length = 120)
    private String timezone;

    @Column(nullable = false)
    private Instant scannedAt;

    protected QrScanEvent() {
    }

    public QrScanEvent(
            QrCode qrCode,
            String visitorId,
            boolean uniqueVisitor,
            String ipAddress,
            String userAgent,
            GeoIpService.GeoLocation location
    ) {
        this.id = UUID.randomUUID();
        this.qrCode = qrCode;
        this.visitorId = visitorId;
        this.uniqueVisitor = uniqueVisitor;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.deviceType = UserAgentClassifier.deviceType(userAgent);
        applyLocation(location);
        this.scannedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public QrCode getQrCode() {
        return qrCode;
    }

    public String getVisitorId() {
        return visitorId;
    }

    public boolean isUniqueVisitor() {
        return uniqueVisitor;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public String getDeviceType() {
        return deviceType == null ? UserAgentClassifier.deviceType(userAgent) : deviceType;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public String getCountryName() {
        return countryName;
    }

    public String getRegion() {
        return region;
    }

    public String getCity() {
        return city;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public String getTimezone() {
        return timezone;
    }

    public Instant getScannedAt() {
        return scannedAt;
    }

    private void applyLocation(GeoIpService.GeoLocation location) {
        if (location == null) {
            return;
        }
        this.countryCode = location.countryCode();
        this.countryName = location.countryName();
        this.region = location.region();
        this.city = location.city();
        this.latitude = location.latitude();
        this.longitude = location.longitude();
        this.timezone = location.timezone();
    }
}
