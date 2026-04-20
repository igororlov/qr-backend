package no.benidorm.qr.publicapi;

import java.util.UUID;
import no.benidorm.qr.common.NotFoundException;
import no.benidorm.qr.company.Company;
import no.benidorm.qr.company.CompanyService;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/companies")
public class PublicCompanyController {
    private final CompanyService companyService;

    public PublicCompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @GetMapping(value = "/{companyId}/logo")
    ResponseEntity<byte[]> logo(@PathVariable UUID companyId) {
        Company company = companyService.getActiveCompany(companyId);
        if (company.getLogoBytes() == null || company.getLogoContentType() == null) {
            throw new NotFoundException("Company logo not found");
        }
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noCache())
                .contentType(MediaType.parseMediaType(company.getLogoContentType()))
                .body(company.getLogoBytes());
    }
}
