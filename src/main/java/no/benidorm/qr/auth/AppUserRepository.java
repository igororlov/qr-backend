package no.benidorm.qr.auth;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUser, UUID> {
    java.util.List<AppUser> findAllByOrderByCreatedAtDesc();

    Optional<AppUser> findByEmailIgnoreCase(String email);

    Optional<AppUser> findByEmailVerificationTokenHash(String tokenHash);

    boolean existsByEmailIgnoreCase(String email);
}
