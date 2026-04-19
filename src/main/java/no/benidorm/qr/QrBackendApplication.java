package no.benidorm.qr;

import no.benidorm.qr.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class QrBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(QrBackendApplication.class, args);
    }
}
