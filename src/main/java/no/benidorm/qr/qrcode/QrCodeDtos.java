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
            @NotBlank @Pattern(regexp = "^#[0-9a-fA-F]{6}$") String textColor,
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
            String textColor,
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
                    resolveLogoUrl(qrCode),
                    qrCode.getButtonColor(),
                    qrCode.getTextColor(),
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

    private static String resolveLogoUrl(QrCode qrCode) {
        if (qrCode.getLogoBytes() != null) {
            return "/api/public/q/" + qrCode.getSlug() + "/logo";
        }
        return qrCode.getLogoUrl();
    }

    public record QrImageStyleRequest(
            @NotBlank @Pattern(regexp = "^#[0-9a-fA-F]{6}$") String foregroundColor,
            @NotBlank @Pattern(regexp = "^#[0-9a-fA-F]{6}$") String backgroundColor,
            Boolean backgroundTransparent,
            Boolean logoEnabled
    ) {
    }

    public record QrImageStyleResponse(
            String foregroundColor,
            String backgroundColor,
            boolean backgroundTransparent,
            boolean logoEnabled,
            boolean imageGenerated,
            Instant imageGeneratedAt
    ) {
        public static QrImageStyleResponse from(QrCode qrCode) {
            return new QrImageStyleResponse(
                    qrCode.getQrForegroundColor(),
                    qrCode.getQrBackgroundColor(),
                    qrCode.isQrBackgroundTransparent(),
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

    public record QrAnalyticsResponse(
            QrAnalyticsSummary scans,
            QrAnalyticsSummary actions,
            List<QrAnalyticsEventResponse> events
    ) {
    }

    public record QrAnalyticsSummary(
            long lastDay,
            long lastWeek,
            long lastMonth
    ) {
    }

    public record QrAnalyticsEventResponse(
            UUID id,
            String type,
            Instant occurredAt,
            String visitorId,
            String ipAddress,
            String userAgent,
            String deviceType,
            String countryCode,
            String countryName,
            String region,
            String city,
            Double latitude,
            Double longitude,
            String timezone,
            Boolean uniqueVisitor,
            UUID actionId,
            String actionLabel,
            QrActionType actionType
    ) {
        public static QrAnalyticsEventResponse scan(QrScanEvent event) {
            return new QrAnalyticsEventResponse(
                    event.getId(),
                    "SCAN",
                    event.getScannedAt(),
                    event.getVisitorId(),
                    event.getIpAddress(),
                    event.getUserAgent(),
                    event.getDeviceType(),
                    event.getCountryCode(),
                    event.getCountryName(),
                    event.getRegion(),
                    event.getCity(),
                    event.getLatitude(),
                    event.getLongitude(),
                    event.getTimezone(),
                    event.isUniqueVisitor(),
                    null,
                    null,
                    null
            );
        }

        public static QrAnalyticsEventResponse click(QrActionClickEvent event) {
            QrAction action = event.getAction();
            return new QrAnalyticsEventResponse(
                    event.getId(),
                    "ACTION_CLICK",
                    event.getClickedAt(),
                    event.getVisitorId(),
                    event.getIpAddress(),
                    event.getUserAgent(),
                    event.getDeviceType(),
                    event.getCountryCode(),
                    event.getCountryName(),
                    event.getRegion(),
                    event.getCity(),
                    event.getLatitude(),
                    event.getLongitude(),
                    event.getTimezone(),
                    null,
                    action.getId(),
                    action.getLabel(),
                    action.getType()
            );
        }
    }
}
