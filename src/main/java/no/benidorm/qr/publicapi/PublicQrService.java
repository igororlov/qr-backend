package no.benidorm.qr.publicapi;

import java.util.UUID;
import no.benidorm.qr.common.BadRequestException;
import no.benidorm.qr.common.NotFoundException;
import no.benidorm.qr.publicapi.PublicDtos.PublicQrResponse;
import no.benidorm.qr.publicapi.PublicDtos.SubmitFormRequest;
import no.benidorm.qr.publicapi.PublicDtos.SubmitFormResponse;
import no.benidorm.qr.publicapi.PublicDtos.TrackClickResponse;
import no.benidorm.qr.qrcode.QrAction;
import no.benidorm.qr.qrcode.QrActionRepository;
import no.benidorm.qr.qrcode.QrActionType;
import no.benidorm.qr.qrcode.QrCode;
import no.benidorm.qr.qrcode.QrCodeRepository;
import no.benidorm.qr.qrcode.QrFormSubmission;
import no.benidorm.qr.qrcode.QrFormSubmissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PublicQrService {
    private final QrCodeRepository qrCodes;
    private final QrActionRepository actions;
    private final QrFormSubmissionRepository submissions;
    private final SubmissionMailService submissionMailService;

    public PublicQrService(
            QrCodeRepository qrCodes,
            QrActionRepository actions,
            QrFormSubmissionRepository submissions,
            SubmissionMailService submissionMailService
    ) {
        this.qrCodes = qrCodes;
        this.actions = actions;
        this.submissions = submissions;
        this.submissionMailService = submissionMailService;
    }

    @Transactional
    public PublicQrResponse get(String slug) {
        QrCode qrCode = getActiveQr(slug);
        qrCode.incrementScanCount();
        return PublicQrResponse.from(qrCode);
    }

    @Transactional
    public TrackClickResponse trackClick(String slug, UUID actionId) {
        QrCode qrCode = getActiveQr(slug);
        QrAction action = actions.findByIdAndQrCode(actionId, qrCode)
                .orElseThrow(() -> new NotFoundException("Action not found"));
        if (!action.isActive()) {
            throw new NotFoundException("Action not found");
        }
        action.incrementClickCount();
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
}

