package no.benidorm.qr.publicapi;

import no.benidorm.qr.common.BadRequestException;
import no.benidorm.qr.config.AppProperties;
import no.benidorm.qr.qrcode.QrFormSubmission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class SubmissionMailService {
    private static final Logger log = LoggerFactory.getLogger(SubmissionMailService.class);
    private static final String RESEND_URL = "https://api.resend.com/emails";

    private final RestClient restClient;
    private final AppProperties properties;

    public SubmissionMailService(AppProperties properties) {
        this.restClient = RestClient.builder().build();
        this.properties = properties;
    }

    public void send(QrFormSubmission submission) {
        String apiKey = properties.resendApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            throw new BadRequestException("Email delivery is not configured");
        }

        String ownerEmail = submission.getQrCode().getCompany().getOwner().getEmail();
        ResendEmailRequest request = new ResendEmailRequest(
                properties.mailFrom(),
                ownerEmail,
                subject(submission),
                body(submission),
                replyTo(submission)
        );

        try {
            log.info("Sending QR form submission {} to {} with Resend API", submission.getId(), ownerEmail);
            restClient.post()
                    .uri(RESEND_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Sent QR form submission {} to {}", submission.getId(), ownerEmail);
        } catch (RestClientException ex) {
            log.error("Could not send QR form submission {} to {}", submission.getId(), ownerEmail, ex);
            throw ex;
        }
    }

    private String subject(QrFormSubmission submission) {
        return "New QR form submission: " + submission.getQrCode().getTitle();
    }

    private String body(QrFormSubmission submission) {
        return """
                New QR form submission

                Company: %s
                QR: %s
                Name: %s
                Email: %s
                Phone: %s

                Message:
                %s
                """.formatted(
                submission.getQrCode().getCompany().getName(),
                submission.getQrCode().getTitle(),
                nullToDash(submission.getSenderName()),
                nullToDash(submission.getSenderEmail()),
                nullToDash(submission.getSenderPhone()),
                submission.getMessage()
        );
    }

    private String[] replyTo(QrFormSubmission submission) {
        if (submission.getSenderEmail() == null || submission.getSenderEmail().isBlank()) {
            return null;
        }
        return new String[]{submission.getSenderEmail().trim()};
    }

    private String nullToDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private record ResendEmailRequest(
            String from,
            String to,
            String subject,
            String text,
            String[] reply_to
    ) {
    }
}
