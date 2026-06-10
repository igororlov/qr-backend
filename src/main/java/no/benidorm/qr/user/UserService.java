package no.benidorm.qr.user;

import java.util.List;
import java.util.UUID;
import no.benidorm.qr.auth.AppUser;
import no.benidorm.qr.auth.AppUserRepository;
import no.benidorm.qr.common.BadRequestException;
import no.benidorm.qr.common.NotFoundException;
import no.benidorm.qr.company.CompanyRepository;
import no.benidorm.qr.user.UserDtos.UserCreateRequest;
import no.benidorm.qr.user.UserDtos.UserResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final AppUserRepository users;
    private final CompanyRepository companies;
    private final PasswordEncoder passwordEncoder;

    public UserService(AppUserRepository users, CompanyRepository companies, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.companies = companies;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> list() {
        return users.findAllByOrderByCreatedAtDesc().stream()
                .map(user -> UserResponse.from(user, companies.findByOwnerOrderByCreatedAtDesc(user)))
                .toList();
    }

    @Transactional
    public UserResponse create(UserCreateRequest request) {
        if (users.existsByEmailIgnoreCase(request.email())) {
            throw new BadRequestException("User email is already used");
        }
        AppUser user = users.save(new AppUser(
                request.email(),
                passwordEncoder.encode(request.password()),
                request.fullName(),
                request.role()
        ));
        return UserResponse.from(user);
    }

    @Transactional
    public void delete(AppUser currentUser, UUID userId) {
        AppUser user = users.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
        if (user.getId().equals(currentUser.getId())) {
            throw new BadRequestException("You cannot delete your own user");
        }
        if (!companies.findByOwnerOrderByCreatedAtDesc(user).isEmpty()) {
            throw new BadRequestException("User owns companies and cannot be deleted");
        }
        users.delete(user);
    }
}
