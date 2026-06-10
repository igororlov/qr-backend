package no.benidorm.qr.qrcode;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QrScanEventRepository extends JpaRepository<QrScanEvent, UUID> {
    boolean existsByQrCodeAndVisitorId(QrCode qrCode, String visitorId);

    long countByQrCodeAndScannedAtAfter(QrCode qrCode, Instant scannedAt);

    List<QrScanEvent> findByQrCode(QrCode qrCode);

    List<QrScanEvent> findByQrCodeAndVisitorId(QrCode qrCode, String visitorId);
}
