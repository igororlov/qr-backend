package no.benidorm.qr.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public final class AuthDtos {
    private AuthDtos() {
    }

    public record LoginRequest(
            @Email @NotBlank String email,
            @NotBlank String password
    ) {
    }

    public record LoginResponse(
            String token,
            UserResponse user
    ) {
    }

    public record UserResponse(
            UUID id,
            String email,
            String fullName,
            UserRole role
    ) {
        public static UserResponse from(AppUser user) {
            return new UserResponse(user.getId(), user.getEmail(), user.getFullName(), user.getRole());
        }
    }
}

