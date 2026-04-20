package no.benidorm.qr.company;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import no.benidorm.qr.company.CompanyDtos.CompanyRequest;
import no.benidorm.qr.company.CompanyDtos.CompanyResponse;
import no.benidorm.qr.security.CurrentUser;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {
    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @GetMapping
    List<CompanyResponse> list(Authentication authentication) {
        return companyService.list(CurrentUser.from(authentication));
    }

    @PostMapping
    CompanyResponse create(Authentication authentication, @Valid @RequestBody CompanyRequest request) {
        return companyService.create(CurrentUser.from(authentication), request);
    }

    @PutMapping("/{companyId}")
    CompanyResponse update(
            Authentication authentication,
            @PathVariable UUID companyId,
            @Valid @RequestBody CompanyRequest request
    ) {
        return companyService.update(CurrentUser.from(authentication), companyId, request);
    }

    @DeleteMapping("/{companyId}")
    ResponseEntity<Void> delete(Authentication authentication, @PathVariable UUID companyId) {
        companyService.delete(CurrentUser.from(authentication), companyId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{companyId}/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    CompanyResponse uploadLogo(
            Authentication authentication,
            @PathVariable UUID companyId,
            @RequestPart("file") MultipartFile file
    ) {
        return companyService.uploadLogo(CurrentUser.from(authentication), companyId, file);
    }
}
