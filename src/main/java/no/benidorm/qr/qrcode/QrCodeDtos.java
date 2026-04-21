package no.benidorm.qr.qrcode;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public final class QrCodeDtos {
    private QrCodeDtos() {
    }

    public record QrCodeRequest(
            @NotBlank @Pattern(regexp = "^[a-z0-9-]{3,120}$") String slug,
            @NotBlank @Size(max = 160) String title,
            @Size(max = 240) String subtitle,
            @Size(max = 120) String label,
            @Size(max = 2000) String logoUrl,
            Boolean active,
            @NotBlank @Pattern(regexp = "^#[0-9a-fA-F]{6}$") String buttonColor,
            @Valid QrImageStyleRequest imageStyle,
            @NotEmpty @Size(min = 1, max = 10) List<@Valid QrActionRequest> actions
    ) {
    }

    public record QrActionRequest(
            int position,
            @NotBlank @Size(max = 120) String label,
            @NotNull QrActionType type,
            @NotBlank @Size(max = 2000) String value,
            Boolean active
    ) {
    }

    public record QrCodeResponse(
            UUID id,
            UUID companyId,
            String companyName,
            String slug,
            String title,
            String subtitle,
            String label,
            String logoUrl,
            String buttonColor,
            boolean active,
            long scanCount,
            QrImageStyleResponse imageStyle,
            List<QrActionResponse> actions,
            Instant createdAt,
            Instant updatedAt
    ) {
        public static QrCodeResponse from(QrCode qrCode) {
            return new QrCodeResponse(
                    qrCode.getId(),
                    qrCode.getCompany().getId(),
                    qrCode.getCompany().getName(),
                    qrCode.getSlug(),
                    qrCode.getTitle(),
                    qrCode.getSubtitle(),
                    qrCode.getLabel(),
                    qrCode.getLogoUrl(),
                    qrCode.getButtonColor(),
                    qrCode.isActive(),
                    qrCode.getScanCount(),
                    QrImageStyleResponse.from(qrCode),
                    qrCode.getActions().stream()
                            .sorted(Comparator.comparingInt(QrAction::getPosition))
                            .map(QrActionResponse::from)
                            .toList(),
                    qrCode.getCreatedAt(),
                    qrCode.getUpdatedAt()
            );
        }
    }

    public record QrImageStyleRequest(
            @NotBlank @Pattern(regexp = "^#[0-9a-fA-F]{6}$") String foregroundColor,
            @NotBlank @Pattern(regexp = "^#[0-9a-fA-F]{6}$") String backgroundColor,
            Boolean logoEnabled
    ) {
    }

    public record QrImageStyleResponse(
            String foregroundColor,
            String backgroundColor,
            boolean logoEnabled,
            boolean imageGenerated,
            Instant imageGeneratedAt
    ) {
        public static QrImageStyleResponse from(QrCode qrCode) {
            return new QrImageStyleResponse(
                    qrCode.getQrForegroundColor(),
                    qrCode.getQrBackgroundColor(),
                    qrCode.isQrLogoEnabled(),
                    qrCode.getQrImagePng() != null,
                    qrCode.getQrImageGeneratedAt()
            );
        }
    }

    public record QrActionResponse(
            UUID id,
            int position,
            String label,
            QrActionType type,
            String value,
            boolean active,
            long clickCount
    ) {
        public static QrActionResponse from(QrAction action) {
            return new QrActionResponse(
                    action.getId(),
                    action.getPosition(),
                    action.getLabel(),
                    action.getType(),
                    action.getValue(),
                    action.isActive(),
                    action.getClickCount()
            );
        }
    }
}
