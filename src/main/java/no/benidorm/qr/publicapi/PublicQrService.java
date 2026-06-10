package no.benidorm.qr.publicapi;

import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import no.benidorm.qr.common.BadRequestException;
import no.benidorm.qr.common.NotFoundException;
import no.benidorm.qr.publicapi.PublicDtos.PublicQrResponse;
import no.benidorm.qr.publicapi.PublicDtos.SubmitFormRequest;
import no.benidorm.qr.publicapi.PublicDtos.SubmitFormResponse;
import no.benidorm.qr.publicapi.PublicDtos.TrackClickRequest;
import no.benidorm.qr.publicapi.PublicDtos.TrackClickResponse;
import no.benidorm.qr.publicapi.PublicDtos.TrackScanRequest;
import no.benidorm.qr.publicapi.PublicDtos.TrackScanResponse;
import no.benidorm.qr.qrcode.QrAction;
import no.benidorm.qr.qrcode.QrActionClickEvent;
import no.benidorm.qr.qrcode.QrActionClickEventRepository;
import no.benidorm.qr.qrcode.QrActionRepository;
import no.benidorm.qr.qrcode.QrActionType;
import no.benidorm.qr.qrcode.QrCode;
import no.benidorm.qr.qrcode.QrCodeRepository;
import no.benidorm.qr.qrcode.GeoIpService;
import no.benidorm.qr.qrcode.QrFormSubmission;
import no.benidorm.qr.qrcode.QrFormSubmissionRepository;
import no.benidorm.qr.qrcode.QrScanEvent;
import no.benidorm.qr.qrcode.QrScanEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PublicQrService {
    private final QrCodeRepository qrCodes;
    private final QrActionRepository actions;
    private final QrFormSubmissionRepository submissions;
    private final QrScanEventRepository scanEvents;
    private final QrActionClickEventRepository clickEvents;
    private final GeoIpService geoIpService;
    private final SubmissionMailService submissionMailService;

    public PublicQrService(
            QrCodeRepository qrCodes,
            QrActionRepository actions,
            QrFormSubmissionRepository submissions,
            QrScanEventRepository scanEvents,
            QrActionClickEventRepository clickEvents,
            GeoIpService geoIpService,
            SubmissionMailService submissionMailService
    ) {
        this.qrCodes = qrCodes;
        this.actions = actions;
        this.submissions = submissions;
        this.scanEvents = scanEvents;
        this.clickEvents = clickEvents;
        this.geoIpService = geoIpService;
        this.submissionMailService = submissionMailService;
    }

    @Transactional(readOnly = true)
    public PublicQrResponse get(String slug) {
        QrCode qrCode = getActiveQr(slug);
        return PublicQrResponse.from(qrCode);
    }

    @Transactional
    public TrackScanResponse trackScan(String slug, TrackScanRequest request, HttpServletRequest servletRequest) {
        QrCode qrCode = getActiveQr(slug);
        String visitorId = normalizeVisitorId(request == null ? null : request.visitorId());
        boolean uniqueVisitor = visitorId != null && !scanEvents.existsByQrCodeAndVisitorId(qrCode, visitorId);
        String ipAddress = resolveIpAddress(servletRequest);
        String userAgent = truncate(servletRequest.getHeader("User-Agent"), 4000);
        qrCode.incrementScanCount();
        scanEvents.save(new QrScanEvent(
                qrCode,
                visitorId,
                uniqueVisitor,
                ipAddress,
                userAgent,
                geoIpService.lookup(ipAddress)
        ));
        return new TrackScanResponse("ok", uniqueVisitor);
    }

    @Transactional
    public TrackClickResponse trackClick(String slug, UUID actionId, TrackClickRequest request, HttpServletRequest servletRequest) {
        QrCode qrCode = getActiveQr(slug);
        QrAction action = actions.findByIdAndQrCode(actionId, qrCode)
                .orElseThrow(() -> new NotFoundException("Action not found"));
        if (!action.isActive()) {
            throw new NotFoundException("Action not found");
        }
        String ipAddress = resolveIpAddress(servletRequest);
        String userAgent = truncate(servletRequest.getHeader("User-Agent"), 4000);
        action.incrementClickCount();
        clickEvents.save(new QrActionClickEvent(
                qrCode,
                action,
                normalizeVisitorId(request == null ? null : request.visitorId()),
                ipAddress,
                userAgent,
                geoIpService.lookup(ipAddress)
        ));
        return new TrackClickResponse("ok");
    }

    @Transactional
    public SubmitFormResponse submitForm(String slug, SubmitFormRequest request) {
        QrCode qrCode = getActiveQr(slug);
        boolean formEnabled = qrCode.getActions().stream()
                .anyMatch(action -> action.isActive() && action.getType() == QrActionType.FORM);
        if (!formEnabled) {
            throw new BadRequestException("Form action is not enabled for this QR code");
        }
        QrFormSubmission submission = submissions.save(new QrFormSubmission(
                qrCode,
                request.senderName(),
                request.senderEmail(),
                request.senderPhone(),
                request.message()
        ));
        submissionMailService.send(submission);
        return new SubmitFormResponse(submission.getId(), "sent");
    }

    private QrCode getActiveQr(String slug) {
        return qrCodes.findWithActionsBySlugAndActiveTrue(slug)
                .filter(qrCode -> qrCode.getCompany().isActive())
                .orElseThrow(() -> new NotFoundException("QR code not found"));
    }

    private static String normalizeVisitorId(String visitorId) {
        String trimmed = visitorId == null ? "" : visitorId.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return truncate(trimmed, 100);
    }

    private static String resolveIpAddress(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return truncate(forwardedFor.split(",")[0].trim(), 45);
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return truncate(realIp.trim(), 45);
        }
        return truncate(request.getRemoteAddr(), 45);
    }

    private static String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
