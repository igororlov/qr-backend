package no.benidorm.qr.qrcode;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QrActionRepository extends JpaRepository<QrAction, UUID> {
    Optional<QrAction> findByIdAndQrCode(UUID id, QrCode qrCode);
}

