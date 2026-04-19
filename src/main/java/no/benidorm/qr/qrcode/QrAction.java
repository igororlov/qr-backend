package no.benidorm.qr.qrcode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import no.benidorm.qr.common.AuditableEntity;

@Entity
@Table(name = "qr_action")
public class QrAction extends AuditableEntity {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "qr_code_id")
    private QrCode qrCode;

    @Column(nullable = false)
    private int position;

    @Column(nullable = false, length = 120)
    private String label;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private QrActionType type;

    @Column(nullable = false, columnDefinition = "text")
    private String value;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private long clickCount;

    protected QrAction() {
    }

    public QrAction(int position, String label, QrActionType type, String value, boolean active) {
        this.id = UUID.randomUUID();
        this.position = position;
        this.label = label;
        this.type = type;
        this.value = value;
        this.active = active;
    }

    void attachTo(QrCode qrCode) {
        this.qrCode = qrCode;
    }

    public UUID getId() {
        return id;
    }

    public int getPosition() {
        return position;
    }

    public String getLabel() {
        return label;
    }

    public QrActionType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public boolean isActive() {
        return active;
    }

    public long getClickCount() {
        return clickCount;
    }

    public void incrementClickCount() {
        clickCount++;
    }
}
