package no.benidorm.qr.qrcode;

import jakarta.persistence.EntityManager;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import no.benidorm.qr.auth.AppUser;
import no.benidorm.qr.common.BadRequestException;
import no.benidorm.qr.common.NotFoundException;
import no.benidorm.qr.company.Company;
import no.benidorm.qr.company.CompanyService;
import no.benidorm.qr.config.AppProperties;
import no.benidorm.qr.qrcode.QrCodeDtos.QrActionRequest;
import no.benidorm.qr.qrcode.QrCodeDtos.QrCodeRequest;
import no.benidorm.qr.qrcode.QrCodeDtos.QrCodeResponse;
import no.benidorm.qr.qrcode.QrCodeDtos.QrImageStyleRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QrCodeService {
    private final QrCodeRepository qrCodes;
    private final CompanyService companyService;
    private final AppProperties properties;
    private final QrImageService qrImageService;
    private final EntityManager entityManager;

    public QrCodeService(
            QrCodeRepository qrCodes,
            CompanyService companyService,
            AppProperties properties,
            QrImageService qrImageService,
            EntityManager entityManager
    ) {
        this.qrCodes = qrCodes;
        this.companyService = companyService;
        this.properties = properties;
        this.qrImageService = qrImageService;
        this.entityManager = entityManager;
    }

    @Transactional(readOnly = true)
    public List<QrCodeResponse> list(AppUser user, UUID companyId) {
        Company company = companyService.getOwnedCompany(user, companyId);
        return qrCodes.findByCompanyOrderByCreatedAtDesc(company).stream()
                .map(QrCodeResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public QrCodeResponse get(AppUser user, UUID companyId, UUID qrCodeId) {
        Company company = companyService.getOwnedCompany(user, companyId);
        QrCode qrCode = getByCompany(company, qrCodeId);
        return QrCodeResponse.from(qrCode);
    }

    @Transactional
    public QrCodeResponse create(AppUser user, UUID companyId, QrCodeRequest request) {
        Company company = companyService.getOwnedCompany(user, companyId);
        ensureSlugAvailable(request.slug(), null);
        QrCode qrCode = new QrCode(
                company,
                request.slug(),
                request.title(),
                request.subtitle(),
                request.label(),
                request.logoUrl()
        );
        qrCode.update(request.slug(), request.title(), request.subtitle(), request.label(), request.logoUrl(), activeOrTrue(request.active()));
        qrCode.updateButtonColor(request.buttonColor().toLowerCase());
        applyImageStyle(qrCode, request);
        qrCode.replaceActions(toActions(request.actions()));
        return QrCodeResponse.from(qrCodes.save(qrCode));
    }

    @Transactional
    public QrCodeResponse update(AppUser user, UUID companyId, UUID qrCodeId, QrCodeRequest request) {
        Company company = companyService.getOwnedCompany(user, companyId);
        QrCode qrCode = getByCompany(company, qrCodeId);
        ensureSlugAvailable(request.slug(), qrCodeId);
        qrCode.update(request.slug(), request.title(), request.subtitle(), request.label(), request.logoUrl(), activeOrTrue(request.active()));
        qrCode.updateButtonColor(request.buttonColor().toLowerCase());
        applyImageStyle(qrCode, request);
        qrCode.clearActions();
        entityManager.flush();
        qrCode.appendActions(toActions(request.actions()));
        return QrCodeResponse.from(qrCode);
    }

    @Transactional
    public QrCodeResponse generateImage(AppUser user, UUID companyId, UUID qrCodeId, QrImageStyleRequest request) {
        Company company = companyService.getOwnedCompany(user, companyId);
        QrCode qrCode = getByCompany(company, qrCodeId);
        qrCode.updateImageStyle(
                request.foregroundColor().toLowerCase(),
                request.backgroundColor().toLowerCase(),
                activeOrTrue(request.logoEnabled())
        );
        qrCode.storeQrImage(qrImageService.png(qrCode, publicUrl(qrCode)));
        return QrCodeResponse.from(qrCode);
    }

    @Transactional
    public byte[] getOrCreateImage(AppUser user, UUID companyId, UUID qrCodeId) {
        Company company = companyService.getOwnedCompany(user, companyId);
        QrCode qrCode = getByCompany(company, qrCodeId);
        if (qrCode.getQrImagePng() == null) {
            qrCode.storeQrImage(qrImageService.png(qrCode, publicUrl(qrCode)));
        }
        return qrCode.getQrImagePng();
    }

    @Transactional(readOnly = true)
    public QrCode getPublicBySlug(String slug) {
        return qrCodes.findWithActionsBySlugAndActiveTrue(slug)
                .orElseThrow(() -> new NotFoundException("QR code not found"));
    }

    @Transactional
    public byte[] getOrCreatePublicImage(String slug) {
        QrCode qrCode = getPublicBySlug(slug);
        if (qrCode.getQrImagePng() == null) {
            qrCode.storeQrImage(qrImageService.png(qrCode, publicUrl(qrCode)));
        }
        return qrCode.getQrImagePng();
    }

    @Transactional(readOnly = true)
    public QrCode getByCompany(Company company, UUID qrCodeId) {
        return qrCodes.findWithActionsByIdAndCompany(qrCodeId, company)
                .orElseThrow(() -> new NotFoundException("QR code not found"));
    }

    public String publicUrl(QrCode qrCode) {
        return properties.publicBaseUrl().replaceAll("/+$", "") + "/q/" + qrCode.getSlug();
    }

    private List<QrAction> toActions(List<QrActionRequest> requests) {
        validateActions(requests);
        return requests.stream()
                .sorted(Comparator.comparingInt(QrActionRequest::position))
                .map(request -> new QrAction(
                        request.position(),
                        request.label(),
                        request.type(),
                        request.value(),
                        activeOrTrue(request.active())
                ))
                .toList();
    }

    private void validateActions(List<QrActionRequest> actions) {
        Set<Integer> positions = new HashSet<>();
        for (QrActionRequest action : actions) {
            if (action.position() < 1 || action.position() > 10) {
                throw new BadRequestException("Action position must be between 1 and 10");
            }
            if (!positions.add(action.position())) {
                throw new BadRequestException("Action positions must be unique");
            }
        }
    }

    private void ensureSlugAvailable(String slug, UUID currentQrCodeId) {
        qrCodes.findBySlug(slug).ifPresent(qrCode -> {
            if (!qrCode.getId().equals(currentQrCodeId)) {
                throw new BadRequestException("QR slug is already used");
            }
        });
    }

    private boolean activeOrTrue(Boolean active) {
        return active == null || active;
    }

    private void applyImageStyle(QrCode qrCode, QrCodeRequest request) {
        if (request.imageStyle() == null) {
            return;
        }
        qrCode.updateImageStyle(
                request.imageStyle().foregroundColor().toLowerCase(),
                request.imageStyle().backgroundColor().toLowerCase(),
                activeOrTrue(request.imageStyle().logoEnabled())
        );
    }
}
