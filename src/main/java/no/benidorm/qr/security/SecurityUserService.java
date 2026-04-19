package no.benidorm.qr.security;

import no.benidorm.qr.auth.AppUserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class SecurityUserService implements UserDetailsService {
    private final AppUserRepository users;

    public SecurityUserService(AppUserRepository users) {
        this.users = users;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return users.findByEmailIgnoreCase(username)
                .map(SecurityUser::new)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}

