package no.benidorm.qr.company;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

public final class CompanyDtos {
    private CompanyDtos() {
    }

    public record CompanyRequest(
            @NotBlank @Size(max = 160) String name,
            @NotBlank @Pattern(regexp = "^[a-z0-9-]{3,120}$") String slug,
            @Size(max = 2000) String logoUrl,
            @Pattern(regexp = "^$|^[0-9]{9}$") String organizationNumber,
            @Size(max = 255) String addressLine,
            @Size(max = 20) String postalCode,
            @Size(max = 120) String postalPlace,
            Boolean active,
            UUID ownerUserId
    ) {
    }

    public record CompanyResponse(
            UUID id,
            String name,
            String slug,
            String logoUrl,
            String organizationNumber,
            String addressLine,
            String postalCode,
            String postalPlace,
            boolean active,
            long qrCount,
            UUID ownerUserId,
            String ownerFullName,
            String ownerEmail,
            Instant createdAt,
            Instant updatedAt
    ) {
        public static CompanyResponse from(Company company) {
            return new CompanyResponse(
                    company.getId(),
                    company.getName(),
                    company.getSlug(),
                    resolveLogoUrl(company),
                    company.getOrganizationNumber(),
                    company.getAddressLine(),
                    company.getPostalCode(),
                    company.getPostalPlace(),
                    company.isActive(),
                    0,
                    company.getOwner().getId(),
                    company.getOwner().getFullName(),
                    company.getOwner().getEmail(),
                    company.getCreatedAt(),
                    company.getUpdatedAt()
            );
        }

        public static CompanyResponse from(Company company, long qrCount) {
            return new CompanyResponse(
                    company.getId(),
                    company.getName(),
                    company.getSlug(),
                    resolveLogoUrl(company),
                    company.getOrganizationNumber(),
                    company.getAddressLine(),
                    company.getPostalCode(),
                    company.getPostalPlace(),
                    company.isActive(),
                    qrCount,
                    company.getOwner().getId(),
                    company.getOwner().getFullName(),
                    company.getOwner().getEmail(),
                    company.getCreatedAt(),
                    company.getUpdatedAt()
            );
        }
    }

    private static String resolveLogoUrl(Company company) {
        if (company.getLogoBytes() != null) {
            return "/api/public/companies/" + company.getId() + "/logo";
        }
        return company.getLogoUrl();
    }
}
