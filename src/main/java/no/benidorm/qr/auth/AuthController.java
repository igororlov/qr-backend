package no.benidorm.qr.auth;

import jakarta.validation.Valid;
import no.benidorm.qr.auth.AuthDtos.LoginRequest;
import no.benidorm.qr.auth.AuthDtos.LoginResponse;
import no.benidorm.qr.auth.AuthDtos.UserResponse;
import no.benidorm.qr.security.CurrentUser;
import no.benidorm.qr.security.JwtService;
import no.benidorm.qr.security.SecurityUser;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    LoginResponse login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        AppUser user = ((SecurityUser) authentication.getPrincipal()).user();
        return new LoginResponse(jwtService.createToken(user), UserResponse.from(user));
    }

    @GetMapping("/me")
    UserResponse me(Authentication authentication) {
        return UserResponse.from(CurrentUser.from(authentication));
    }
}

