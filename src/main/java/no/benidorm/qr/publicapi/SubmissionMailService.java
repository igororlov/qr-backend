package no.benidorm.qr.publicapi;

import no.benidorm.qr.qrcode.QrFormSubmission;
import no.benidorm.qr.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class SubmissionMailService {
    private static final Logger log = LoggerFactory.getLogger(SubmissionMailService.class);

    private final JavaMailSender mailSender;
    private final AppProperties properties;

    public SubmissionMailService(JavaMailSender mailSender, AppProperties properties) {
        this.mailSender = mailSender;
        this.properties = properties;
    }

    public void send(QrFormSubmission submission) {
        String ownerEmail = submission.getQrCode().getCompany().getOwner().getEmail();
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(properties.mailFrom());
        message.setTo(ownerEmail);
        message.setSubject("New QR form submission: " + submission.getQrCode().getTitle());
        message.setText("""
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
        ));
        try {
            log.info("Sending QR form submission {} to {}", submission.getId(), ownerEmail);
            mailSender.send(message);
            log.info("Sent QR form submission {} to {}", submission.getId(), ownerEmail);
        } catch (MailException ex) {
            log.error("Could not send QR form submission {} to {}", submission.getId(), ownerEmail, ex);
            throw ex;
        }
    }

    private String nullToDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}
