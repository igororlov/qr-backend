package no.benidorm.qr.publicapi;

import jakarta.validation.Valid;
import java.util.UUID;
import no.benidorm.qr.publicapi.PublicDtos.PublicQrResponse;
import no.benidorm.qr.publicapi.PublicDtos.SubmitFormRequest;
import no.benidorm.qr.publicapi.PublicDtos.SubmitFormResponse;
import no.benidorm.qr.publicapi.PublicDtos.TrackClickResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/q/{slug}")
public class PublicQrController {
    private final PublicQrService publicQrService;

    public PublicQrController(PublicQrService publicQrService) {
        this.publicQrService = publicQrService;
    }

    @GetMapping
    PublicQrResponse get(@PathVariable String slug) {
        return publicQrService.get(slug);
    }

    @PostMapping("/actions/{actionId}/click")
    TrackClickResponse trackClick(@PathVariable String slug, @PathVariable UUID actionId) {
        return publicQrService.trackClick(slug, actionId);
    }

    @PostMapping("/submit-form")
    SubmitFormResponse submitForm(@PathVariable String slug, @Valid @RequestBody SubmitFormRequest request) {
        return publicQrService.submitForm(slug, request);
    }
}
