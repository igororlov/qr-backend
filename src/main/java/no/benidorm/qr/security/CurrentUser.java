package no.benidorm.qr.security;

import no.benidorm.qr.auth.AppUser;
import org.springframework.security.core.Authentication;

public final class CurrentUser {
    private CurrentUser() {
    }

    public static AppUser from(Authentication authentication) {
        return ((SecurityUser) authentication.getPrincipal()).user();
    }
}
