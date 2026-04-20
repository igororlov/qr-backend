package no.benidorm.qr.qrcode;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import no.benidorm.qr.company.Company;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QrCodeRepository extends JpaRepository<QrCode, UUID> {
    List<QrCode> findByCompanyOrderByCreatedAtDesc(Company company);

    void deleteByCompany(Company company);

    @EntityGraph(attributePaths = {"actions", "company"})
    Optional<QrCode> findWithActionsByIdAndCompany(UUID id, Company company);

    @EntityGraph(attributePaths = {"actions", "company"})
    Optional<QrCode> findWithActionsBySlugAndActiveTrue(String slug);

    Optional<QrCode> findBySlug(String slug);

    boolean existsBySlug(String slug);
}
