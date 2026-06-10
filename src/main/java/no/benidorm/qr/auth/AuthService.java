package no.benidorm.qr.auth;

import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import no.benidorm.qr.auth.AuthDtos.RegisterRequest;
import no.benidorm.qr.common.BadRequestException;
import no.benidorm.qr.company.Company;
import no.benidorm.qr.company.CompanyRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private static final Duration EMAIL_VERIFICATION_TTL = Duration.ofHours(24);
    private static final int TOKEN_BYTES = 32;
    private static final List<String> COMPANY_LEGAL_SUFFIXES = List.of(
            "AS", "ASA", "NUF", "ENK", "DA", "ANS", "SA", "BA", "KF", "IKS", "FKF", "KS", "BBL", "SF", "HF", "RHF"
    );

    private final AppUserRepository users;
    private final CompanyRepository companies;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationMailService emailVerificationMailService;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(
            AppUserRepository users,
            CompanyRepository companies,
            PasswordEncoder passwordEncoder,
            EmailVerificationMailService emailVerificationMailService
    ) {
        this.users = users;
        this.companies = companies;
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
        String companyName = normalizeCompanyName(request.companyName());
        Company company = new Company(companyName, uniqueCompanySlug(companyName), null, savedUser);
        company.updateRegistryDetails(
                emptyToNull(request.organizationNumber()),
                emptyToNull(request.addressLine()),
                emptyToNull(request.postalCode()),
                emptyToNull(request.postalPlace())
        );
        companies.save(company);
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

    private String uniqueCompanySlug(String name) {
        String base = slugBase(name);
        String candidate = base;
        int suffix = 2;
        while (companies.existsBySlug(candidate)) {
            candidate = base + "-" + suffix;
            suffix++;
        }
        return candidate;
    }

    private String slugBase(String value) {
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        if (normalized.length() < 3) {
            normalized = (normalized + "-company").replaceAll("(^-|-$)", "");
        }
        if (normalized.length() > 120) {
            normalized = normalized.substring(0, 120).replaceAll("-+$", "");
        }
        return normalized.isBlank() ? "company" : normalized;
    }

    private String normalizeCompanyName(String value) {
        String[] words = value.trim().replaceAll("\\s+", " ").split(" ");
        for (int i = 0; i < words.length; i++) {
            words[i] = normalizeCompanyNameToken(words[i]);
        }
        return String.join(" ", words);
    }

    private String normalizeCompanyNameToken(String token) {
        String[] parts = token.split("-", -1);
        for (int i = 0; i < parts.length; i++) {
            parts[i] = normalizeCompanyNameWord(parts[i]);
        }
        return String.join("-", parts);
    }

    private String normalizeCompanyNameWord(String word) {
        if (word.isBlank()) {
            return word;
        }
        String upper = word.toUpperCase(Locale.ROOT);
        if (COMPANY_LEGAL_SUFFIXES.contains(upper) || Character.isDigit(word.codePointAt(0))) {
            return upper;
        }
        String lower = word.toLowerCase(Locale.forLanguageTag("nb-NO"));
        int firstCodePoint = lower.codePointAt(0);
        int firstLength = Character.charCount(firstCodePoint);
        return new String(Character.toChars(Character.toTitleCase(firstCodePoint))) + lower.substring(firstLength);
    }

    private String emptyToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
