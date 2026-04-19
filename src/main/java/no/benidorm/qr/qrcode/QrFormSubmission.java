package no.benidorm.qr.qrcode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "qr_form_submission")
public class QrFormSubmission {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "qr_code_id")
    private QrCode qrCode;

    @Column(length = 160)
    private String senderName;

    @Column(length = 320)
    private String senderEmail;

    @Column(length = 60)
    private String senderPhone;

    @Column(nullable = false, columnDefinition = "text")
    private String message;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected QrFormSubmission() {
    }

    public QrFormSubmission(QrCode qrCode, String senderName, String senderEmail, String senderPhone, String message) {
        this.id = UUID.randomUUID();
        this.qrCode = qrCode;
        this.senderName = senderName;
        this.senderEmail = senderEmail;
        this.senderPhone = senderPhone;
        this.message = message;
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public QrCode getQrCode() {
        return qrCode;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public String getSenderPhone() {
        return senderPhone;
    }

    public String getMessage() {
        return message;
    }
}
