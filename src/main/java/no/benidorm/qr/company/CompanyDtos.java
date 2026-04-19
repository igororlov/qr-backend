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
            Boolean active
    ) {
    }

    public record CompanyResponse(
            UUID id,
            String name,
            String slug,
            String logoUrl,
            boolean active,
            Instant createdAt,
            Instant updatedAt
    ) {
        public static CompanyResponse from(Company company) {
            return new CompanyResponse(
                    company.getId(),
                    company.getName(),
                    company.getSlug(),
                    company.getLogoUrl(),
                    company.isActive(),
                    company.getCreatedAt(),
                    company.getUpdatedAt()
            );
        }
    }
}
