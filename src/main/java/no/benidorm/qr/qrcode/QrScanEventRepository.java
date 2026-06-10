package no.benidorm.qr.qrcode;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QrScanEventRepository extends JpaRepository<QrScanEvent, UUID> {
    boolean existsByQrCodeAndVisitorId(QrCode qrCode, String visitorId);
}
