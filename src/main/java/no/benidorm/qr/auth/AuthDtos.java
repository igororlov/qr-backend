package no.benidorm.qr.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public final class AuthDtos {
    private AuthDtos() {
    }

    public record LoginRequest(
            @Email @NotBlank String email,
            @NotBlank String password
    ) {
    }

    public record RegisterRequest(
            @Email @NotBlank String email,
            @NotBlank @Size(max = 160) String fullName,
            @NotBlank @Size(max = 160) String companyName,
            @Pattern(regexp = "^$|^[0-9]{9}$") String organizationNumber,
            @Size(max = 255) String addressLine,
            @Size(max = 20) String postalCode,
            @Size(max = 120) String postalPlace,
            @NotBlank @Size(min = 8, max = 200) String password
    ) {
    }

    public record RegisterResponse(
            String email
    ) {
    }

    public record VerifyEmailRequest(
            @NotBlank String token
    ) {
    }

    public record VerifyEmailResponse(
            String status
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
