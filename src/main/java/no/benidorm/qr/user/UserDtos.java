package no.benidorm.qr.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import no.benidorm.qr.auth.AppUser;
import no.benidorm.qr.company.Company;
import no.benidorm.qr.auth.UserRole;

public final class UserDtos {
    private UserDtos() {
    }

    public record UserCreateRequest(
            @Email @NotBlank String email,
            @NotBlank @Size(max = 160) String fullName,
            @NotBlank @Size(min = 8, max = 200) String password,
            @NotNull UserRole role
    ) {
    }

    public record UserResponse(
            UUID id,
            String email,
            String fullName,
            UserRole role,
            boolean enabled,
            List<UserCompanyResponse> companies,
            Instant createdAt,
            Instant updatedAt
    ) {
        public static UserResponse from(AppUser user) {
            return from(user, List.of());
        }

        public static UserResponse from(AppUser user, List<Company> companies) {
            return new UserResponse(
                    user.getId(),
                    user.getEmail(),
                    user.getFullName(),
                    user.getRole(),
                    user.isEnabled(),
                    companies.stream().map(UserCompanyResponse::from).toList(),
                    user.getCreatedAt(),
                    user.getUpdatedAt()
            );
        }
    }

    public record UserCompanyResponse(
            UUID id,
            String name,
            String slug,
            boolean active
    ) {
        public static UserCompanyResponse from(Company company) {
            return new UserCompanyResponse(
                    company.getId(),
                    company.getName(),
                    company.getSlug(),
                    company.isActive()
            );
        }
    }
}
