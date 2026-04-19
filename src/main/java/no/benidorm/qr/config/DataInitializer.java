package no.benidorm.qr.config;

import no.benidorm.qr.auth.AppUser;
import no.benidorm.qr.auth.AppUserRepository;
import no.benidorm.qr.auth.UserRole;
import no.benidorm.qr.company.Company;
import no.benidorm.qr.company.CompanyRepository;
import no.benidorm.qr.qrcode.QrAction;
import no.benidorm.qr.qrcode.QrActionType;
import no.benidorm.qr.qrcode.QrCode;
import no.benidorm.qr.qrcode.QrCodeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final AppProperties properties;
    private final AppUserRepository users;
    private final CompanyRepository companies;
    private final QrCodeRepository qrCodes;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
            AppProperties properties,
            AppUserRepository users,
            CompanyRepository companies,
            QrCodeRepository qrCodes,
            PasswordEncoder passwordEncoder
    ) {
        this.properties = properties;
        this.users = users;
        this.companies = companies;
        this.qrCodes = qrCodes;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (users.existsByEmailIgnoreCase(properties.adminEmail())) {
            return;
        }

        AppUser admin = users.save(new AppUser(
                properties.adminEmail(),
                passwordEncoder.encode(properties.adminPassword()),
                "Demo Admin",
                UserRole.SYSTEM_ADMIN
        ));
        Company company = companies.save(new Company("Benidorm Demo", "benidorm-demo", null, admin));
        QrCode qrCode = new QrCode(
                company,
                "demo",
                "Welcome to Benidorm Demo",
                "Choose what you need",
                "Scan me",
                null
        );
        qrCode.replaceActions(java.util.List.of(
                new QrAction(1, "Open website", QrActionType.LINK, "https://example.com", true),
                new QrAction(2, "Call us", QrActionType.PHONE, "+34123456789", true),
                new QrAction(3, "Send a message", QrActionType.FORM, "contact", true)
        ));
        qrCodes.save(qrCode);
        log.info("Created demo admin {} with password from app.admin-password", properties.adminEmail());
    }
}

