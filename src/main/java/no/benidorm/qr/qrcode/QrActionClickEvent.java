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
@Table(name = "qr_action_click_event")
public class QrActionClickEvent {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "qr_code_id")
    private QrCode qrCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "qr_action_id")
    private QrAction action;

    @Column(length = 100)
    private String visitorId;

    @Column(length = 45)
    private String ipAddress;

    @Column(columnDefinition = "text")
    private String userAgent;

    @Column(nullable = false)
    private Instant clickedAt;

    protected QrActionClickEvent() {
    }

    public QrActionClickEvent(QrCode qrCode, QrAction action, String visitorId, String ipAddress, String userAgent) {
        this.id = UUID.randomUUID();
        this.qrCode = qrCode;
        this.action = action;
        this.visitorId = visitorId;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.clickedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public QrCode getQrCode() {
        return qrCode;
    }

    public QrAction getAction() {
        return action;
    }

    public String getVisitorId() {
        return visitorId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public Instant getClickedAt() {
        return clickedAt;
    }
}
