package no.benidorm.qr.company;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import no.benidorm.qr.auth.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, UUID> {
    List<Company> findAllByOrderByCreatedAtDesc();

    List<Company> findByOwnerOrderByCreatedAtDesc(AppUser owner);

    Optional<Company> findByIdAndOwner(UUID id, AppUser owner);

    Optional<Company> findBySlug(String slug);

    boolean existsBySlug(String slug);
}
