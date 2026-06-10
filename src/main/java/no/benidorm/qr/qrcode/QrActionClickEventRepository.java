package no.benidorm.qr.qrcode;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QrActionClickEventRepository extends JpaRepository<QrActionClickEvent, UUID> {
    long countByQrCodeAndClickedAtAfter(QrCode qrCode, Instant clickedAt);

    List<QrActionClickEvent> findByQrCode(QrCode qrCode);

    List<QrActionClickEvent> findByQrCodeAndVisitorId(QrCode qrCode, String visitorId);
}
