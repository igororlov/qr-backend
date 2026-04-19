package no.benidorm.qr.publicapi;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import no.benidorm.qr.qrcode.QrAction;
import no.benidorm.qr.qrcode.QrActionType;
import no.benidorm.qr.qrcode.QrCode;

public final class PublicDtos {
    private PublicDtos() {
    }

    public record PublicQrResponse(
            UUID id,
            String companyName,
            String companySlug,
            String companyLogoUrl,
            String slug,
            String title,
            String subtitle,
            String label,
            String logoUrl,
            List<PublicActionResponse> actions
    ) {
        public static PublicQrResponse from(QrCode qrCode) {
            return new PublicQrResponse(
                    qrCode.getId(),
                    qrCode.getCompany().getName(),
                    qrCode.getCompany().getSlug(),
                    qrCode.getCompany().getLogoUrl(),
                    qrCode.getSlug(),
                    qrCode.getTitle(),
                    qrCode.getSubtitle(),
                    qrCode.getLabel(),
                    qrCode.getLogoUrl(),
                    qrCode.getActions().stream()
                            .filter(QrAction::isActive)
                            .sorted(Comparator.comparingInt(QrAction::getPosition))
                            .map(PublicActionResponse::from)
                            .toList()
            );
        }
    }

    public record PublicActionResponse(
            UUID id,
            int position,
            String label,
            QrActionType type,
            String value
    ) {
        public static PublicActionResponse from(QrAction action) {
            return new PublicActionResponse(
                    action.getId(),
                    action.getPosition(),
                    action.getLabel(),
                    action.getType(),
                    action.getValue()
            );
        }
    }

    public record SubmitFormRequest(
            @Size(max = 160) String senderName,
            @Email @Size(max = 320) String senderEmail,
            @Size(max = 60) String senderPhone,
            @NotBlank @Size(max = 4000) String message
    ) {
    }

    public record SubmitFormResponse(UUID submissionId, String status) {
    }

    public record TrackClickResponse(String status) {
    }
}

