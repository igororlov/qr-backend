package no.benidorm.qr.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;
import no.benidorm.qr.auth.AppUser;
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
            Instant createdAt,
            Instant updatedAt
    ) {
        public static UserResponse from(AppUser user) {
            return new UserResponse(
                    user.getId(),
                    user.getEmail(),
                    user.getFullName(),
                    user.getRole(),
                    user.isEnabled(),
                    user.getCreatedAt(),
                    user.getUpdatedAt()
            );
        }
    }
}
