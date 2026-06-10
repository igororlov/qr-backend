package no.benidorm.qr.user;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import no.benidorm.qr.security.CurrentUser;
import no.benidorm.qr.user.UserDtos.UserCreateRequest;
import no.benidorm.qr.user.UserDtos.UserResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('SYSTEM_ADMIN')")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    List<UserResponse> list() {
        return userService.list();
    }

    @PostMapping
    UserResponse create(@Valid @RequestBody UserCreateRequest request) {
        return userService.create(request);
    }

    @DeleteMapping("/{userId}")
    ResponseEntity<Void> delete(Authentication authentication, @PathVariable UUID userId) {
        userService.delete(CurrentUser.from(authentication), userId);
        return ResponseEntity.noContent().build();
    }
}
