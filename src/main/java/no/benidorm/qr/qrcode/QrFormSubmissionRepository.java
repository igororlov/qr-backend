package no.benidorm.qr.qrcode;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QrFormSubmissionRepository extends JpaRepository<QrFormSubmission, UUID> {
}

