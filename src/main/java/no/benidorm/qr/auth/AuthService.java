package no.benidorm.qr.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import no.benidorm.qr.auth.AuthDtos.RegisterRequest;
import no.benidorm.qr.common.BadRequestException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private static final Duration EMAIL_VERIFICATION_TTL = Duration.ofHours(24);
    private static final int TOKEN_BYTES = 32;

    private final AppUserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationMailService emailVerificationMailService;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(
            AppUserRepository users,
            PasswordEncoder passwordEncoder,
            EmailVerificationMailService emailVerificationMailService
    ) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.emailVerificationMailService = emailVerificationMailService;
    }

    @Transactional
    public AppUser register(RegisterRequest request) {
        if (users.existsByEmailIgnoreCase(request.email())) {
            throw new BadRequestException("User email is already used");
        }
        String token = createToken();
        AppUser user = new AppUser(
                request.email(),
                passwordEncoder.encode(request.password()),
                request.fullName(),
                UserRole.COMPANY_ADMIN
        );
        user.startEmailVerification(hashToken(token), Instant.now().plus(EMAIL_VERIFICATION_TTL));
        AppUser savedUser = users.save(user);
        emailVerificationMailService.send(savedUser, token, EMAIL_VERIFICATION_TTL);
        return savedUser;
    }

    @Transactional
    public void verifyEmail(String token) {
        AppUser user = users.findByEmailVerificationTokenHash(hashToken(token))
                .orElseThrow(() -> new BadRequestException("Email verification link is invalid"));
        if (user.getEmailVerificationExpiresAt() == null || user.getEmailVerificationExpiresAt().isBefore(Instant.now())) {
            throw new BadRequestException("Email verification link has expired");
        }
        user.verifyEmail();
    }

    private String createToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }
}
