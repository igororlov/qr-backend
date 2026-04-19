package no.benidorm.qr.company;

import java.util.List;
import java.util.UUID;
import no.benidorm.qr.auth.AppUser;
import no.benidorm.qr.auth.UserRole;
import no.benidorm.qr.common.BadRequestException;
import no.benidorm.qr.common.NotFoundException;
import no.benidorm.qr.company.CompanyDtos.CompanyRequest;
import no.benidorm.qr.company.CompanyDtos.CompanyResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompanyService {
    private final CompanyRepository companies;

    public CompanyService(CompanyRepository companies) {
        this.companies = companies;
    }

    @Transactional(readOnly = true)
    public List<CompanyResponse> list(AppUser user) {
        List<Company> result = user.getRole() == UserRole.SYSTEM_ADMIN
                ? companies.findAllByOrderByCreatedAtDesc()
                : companies.findByOwnerOrderByCreatedAtDesc(user);
        return result.stream().map(CompanyResponse::from).toList();
    }

    @Transactional
    public CompanyResponse create(AppUser user, CompanyRequest request) {
        ensureSlugAvailable(request.slug(), null);
        Company company = companies.save(new Company(request.name(), request.slug(), request.logoUrl(), user));
        company.update(request.name(), request.slug(), request.logoUrl(), activeOrTrue(request.active()));
        return CompanyResponse.from(company);
    }

    @Transactional
    public CompanyResponse update(AppUser user, UUID id, CompanyRequest request) {
        Company company = getOwnedCompany(user, id);
        ensureSlugAvailable(request.slug(), id);
        company.update(request.name(), request.slug(), request.logoUrl(), activeOrTrue(request.active()));
        return CompanyResponse.from(company);
    }

    @Transactional(readOnly = true)
    public Company getOwnedCompany(AppUser user, UUID companyId) {
        if (user.getRole() == UserRole.SYSTEM_ADMIN) {
            return companies.findById(companyId).orElseThrow(() -> new NotFoundException("Company not found"));
        }
        return companies.findByIdAndOwner(companyId, user)
                .orElseThrow(() -> new NotFoundException("Company not found"));
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
}
