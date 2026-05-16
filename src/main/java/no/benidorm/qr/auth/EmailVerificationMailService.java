package no.benidorm.qr.auth;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import no.benidorm.qr.common.BadRequestException;
import no.benidorm.qr.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class EmailVerificationMailService {
    private static final Logger log = LoggerFactory.getLogger(EmailVerificationMailService.class);
    private static final String RESEND_URL = "https://api.resend.com/emails";

    private final RestClient restClient;
    private final AppProperties properties;

    public EmailVerificationMailService(AppProperties properties) {
        this.restClient = RestClient.builder().build();
        this.properties = properties;
    }

    public void send(AppUser user, String token, Duration ttl) {
        String apiKey = properties.resendApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            throw new BadRequestException("Email delivery is not configured");
        }

        ResendEmailRequest request = new ResendEmailRequest(
                properties.mailFrom(),
                user.getEmail(),
                "Verify your QR Admin email",
                body(user, token, ttl)
        );

        try {
            log.info("Sending email verification link to {}", user.getEmail());
            restClient.post()
                    .uri(RESEND_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException ex) {
            log.error("Could not send email verification link to {}", user.getEmail(), ex);
            throw ex;
        }
    }

    private String body(AppUser user, String token, Duration ttl) {
        return """
                Hi %s,

                Welcome to QR Admin. Confirm your email address to activate your account:

                %s

                This link is valid for %d hours.

                If you did not create this account, you can ignore this email.
                """.formatted(user.getFullName(), verificationUrl(token), ttl.toHours());
    }

    private String verificationUrl(String token) {
        return properties.publicBaseUrl().replaceAll("/+$", "")
                + "/verify-email?token="
                + URLEncoder.encode(token, StandardCharsets.UTF_8);
    }

    private record ResendEmailRequest(
            String from,
            String to,
            String subject,
            String text
    ) {
    }
}
