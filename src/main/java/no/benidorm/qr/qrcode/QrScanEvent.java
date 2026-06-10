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

    @Column(nullable = false)
    private Instant scannedAt;

    protected QrScanEvent() {
    }

    public QrScanEvent(QrCode qrCode, String visitorId, boolean uniqueVisitor, String ipAddress, String userAgent) {
        this.id = UUID.randomUUID();
        this.qrCode = qrCode;
        this.visitorId = visitorId;
        this.uniqueVisitor = uniqueVisitor;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.scannedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public boolean isUniqueVisitor() {
        return uniqueVisitor;
    }
}
