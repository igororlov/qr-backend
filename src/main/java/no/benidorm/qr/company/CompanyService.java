package no.benidorm.qr.company;

import java.util.List;
import java.util.UUID;
import no.benidorm.qr.auth.AppUser;
import no.benidorm.qr.auth.AppUserRepository;
import no.benidorm.qr.auth.UserRole;
import no.benidorm.qr.common.BadRequestException;
import no.benidorm.qr.common.NotFoundException;
import no.benidorm.qr.company.CompanyDtos.CompanyRequest;
import no.benidorm.qr.company.CompanyDtos.CompanyResponse;
import no.benidorm.qr.qrcode.QrCodeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CompanyService {
    private static final long MAX_LOGO_SIZE_BYTES = 2 * 1024 * 1024;

    private final CompanyRepository companies;
    private final AppUserRepository users;
    private final QrCodeRepository qrCodes;

    public CompanyService(
            CompanyRepository companies,
            AppUserRepository users,
            QrCodeRepository qrCodes
    ) {
        this.companies = companies;
        this.users = users;
        this.qrCodes = qrCodes;
    }

    @Transactional(readOnly = true)
    public List<CompanyResponse> list(AppUser user) {
        List<Company> result = user.getRole() == UserRole.SYSTEM_ADMIN
                ? companies.findAllByOrderByCreatedAtDesc()
                : companies.findByOwnerOrderByCreatedAtDesc(user);
        return result.stream().map(this::toResponse).toList();
    }

    @Transactional
    public CompanyResponse create(AppUser user, CompanyRequest request) {
        ensureSlugAvailable(request.slug(), null);
        AppUser owner = resolveOwner(user, request.ownerUserId());
        Company company = companies.save(new Company(request.name(), request.slug(), request.logoUrl(), owner));
        company.update(request.name(), request.slug(), request.logoUrl(), activeOrTrue(request.active()));
        company.updateRegistryDetails(
                emptyToNull(request.organizationNumber()),
                emptyToNull(request.addressLine()),
                emptyToNull(request.postalCode()),
                emptyToNull(request.postalPlace())
        );
        return toResponse(company);
    }

    @Transactional
    public CompanyResponse update(AppUser user, UUID id, CompanyRequest request) {
        Company company = getOwnedCompany(user, id);
        ensureSlugAvailable(request.slug(), id);
        company.update(request.name(), request.slug(), request.logoUrl(), activeOrTrue(request.active()));
        company.updateRegistryDetails(
                emptyToNull(request.organizationNumber()),
                emptyToNull(request.addressLine()),
                emptyToNull(request.postalCode()),
                emptyToNull(request.postalPlace())
        );
        company.changeOwner(resolveOwner(user, request.ownerUserId()));
        return toResponse(company);
    }

    @Transactional
    public void delete(AppUser user, UUID id) {
        Company company = getOwnedCompany(user, id);
        qrCodes.deleteByCompany(company);
        companies.delete(company);
    }

    @Transactional
    public CompanyResponse uploadLogo(AppUser user, UUID id, MultipartFile file) {
        Company company = getOwnedCompany(user, id);
        validateLogo(file);
        try {
            company.storeLogo(publicLogoUrl(company), file.getContentType(), file.getBytes());
            return toResponse(company);
        } catch (java.io.IOException ex) {
            throw new BadRequestException("Could not read logo file");
        }
    }

    @Transactional(readOnly = true)
    public Company getOwnedCompany(AppUser user, UUID companyId) {
        if (user.getRole() == UserRole.SYSTEM_ADMIN) {
            return companies.findById(companyId).orElseThrow(() -> new NotFoundException("Company not found"));
        }
        return companies.findByIdAndOwner(companyId, user)
                .orElseThrow(() -> new NotFoundException("Company not found"));
    }

    @Transactional(readOnly = true)
    public Company getActiveCompany(UUID companyId) {
        Company company = companies.findById(companyId).orElseThrow(() -> new NotFoundException("Company not found"));
        if (!company.isActive()) {
            throw new NotFoundException("Company not found");
        }
        return company;
    }

    @Transactional(readOnly = true)
    public Company getCompany(UUID companyId) {
        return companies.findById(companyId).orElseThrow(() -> new NotFoundException("Company not found"));
    }

    private void ensureSlugAvailable(String slug, UUID currentCompanyId) {
        companies.findBySlug(slug).ifPresent(company -> {
            if (!company.getId().equals(currentCompanyId)) {
                throw new BadRequestException("Company slug is already used");
            }
        });
    }

    private boolean activeOrTrue(Boolean active) {
        return active == null || active;
    }

    private AppUser resolveOwner(AppUser currentUser, UUID ownerUserId) {
        if (currentUser.getRole() != UserRole.SYSTEM_ADMIN) {
            return currentUser;
        }
        if (ownerUserId == null) {
            return currentUser;
        }
        return users.findById(ownerUserId).orElseThrow(() -> new NotFoundException("Owner user not found"));
    }

    private void validateLogo(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Logo file is required");
        }
        if (file.getSize() > MAX_LOGO_SIZE_BYTES) {
            throw new BadRequestException("Logo file is too large");
        }
        String contentType = file.getContentType();
        if (!List.of("image/png", "image/jpeg", "image/webp").contains(contentType)) {
            throw new BadRequestException("Logo must be a PNG, JPEG, or WebP image");
        }
    }

    private String publicLogoUrl(Company company) {
        return "/api/public/companies/" + company.getId() + "/logo";
    }

    private String emptyToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private CompanyResponse toResponse(Company company) {
        return CompanyResponse.from(company, qrCodes.countByCompany(company));
    }
}
