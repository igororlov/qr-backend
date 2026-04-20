package no.benidorm.qr.qrcode;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import no.benidorm.qr.qrcode.QrCodeDtos.QrCodeRequest;
import no.benidorm.qr.qrcode.QrCodeDtos.QrCodeResponse;
import no.benidorm.qr.qrcode.QrCodeDtos.QrImageStyleRequest;
import no.benidorm.qr.security.CurrentUser;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/companies/{companyId}/qr-codes")
public class QrCodeController {
    private final QrCodeService qrCodeService;

    public QrCodeController(QrCodeService qrCodeService) {
        this.qrCodeService = qrCodeService;
    }

    @GetMapping
    List<QrCodeResponse> list(Authentication authentication, @PathVariable UUID companyId) {
        return qrCodeService.list(CurrentUser.from(authentication), companyId);
    }

    @GetMapping("/{qrCodeId}")
    QrCodeResponse get(Authentication authentication, @PathVariable UUID companyId, @PathVariable UUID qrCodeId) {
        return qrCodeService.get(CurrentUser.from(authentication), companyId, qrCodeId);
    }

    @PostMapping
    QrCodeResponse create(
            Authentication authentication,
            @PathVariable UUID companyId,
            @Valid @RequestBody QrCodeRequest request
    ) {
        return qrCodeService.create(CurrentUser.from(authentication), companyId, request);
    }

    @PutMapping("/{qrCodeId}")
    QrCodeResponse update(
            Authentication authentication,
            @PathVariable UUID companyId,
            @PathVariable UUID qrCodeId,
            @Valid @RequestBody QrCodeRequest request
    ) {
        return qrCodeService.update(CurrentUser.from(authentication), companyId, qrCodeId, request);
    }

    @PostMapping("/{qrCodeId}/image")
    QrCodeResponse generateImage(
            Authentication authentication,
            @PathVariable UUID companyId,
            @PathVariable UUID qrCodeId,
            @Valid @RequestBody QrImageStyleRequest request
    ) {
        return qrCodeService.generateImage(CurrentUser.from(authentication), companyId, qrCodeId, request);
    }

    @GetMapping(value = "/{qrCodeId}/png", produces = MediaType.IMAGE_PNG_VALUE)
    ResponseEntity<byte[]> png(Authentication authentication, @PathVariable UUID companyId, @PathVariable UUID qrCodeId) {
        byte[] png = qrCodeService.getOrCreateImage(CurrentUser.from(authentication), companyId, qrCodeId);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(png);
    }
}
