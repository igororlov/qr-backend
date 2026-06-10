package no.benidorm.qr.qrcode;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.NamedRecord;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import jakarta.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import no.benidorm.qr.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class GeoIpService {
    private static final Logger LOGGER = LoggerFactory.getLogger(GeoIpService.class);

    private final DatabaseReader reader;

    public GeoIpService(AppProperties properties) {
        this.reader = createReader(properties.geoIp() == null ? null : properties.geoIp().cityDbPath());
    }

    public GeoLocation lookup(String ipAddress) {
        if (reader == null || ipAddress == null || ipAddress.isBlank()) {
            return GeoLocation.empty();
        }
        try {
            InetAddress address = InetAddress.getByName(ipAddress);
            if (isPrivateAddress(address)) {
                return GeoLocation.empty();
            }
            CityResponse response = reader.city(address);
            return new GeoLocation(
                    response.country().isoCode(),
                    name(response.country()),
                    name(response.mostSpecificSubdivision()),
                    name(response.city()),
                    response.location().latitude(),
                    response.location().longitude(),
                    response.location().timeZone()
            );
        } catch (GeoIp2Exception | IOException | RuntimeException ex) {
            return GeoLocation.empty();
        }
    }

    @PreDestroy
    public void close() throws IOException {
        if (reader != null) {
            reader.close();
        }
    }

    private DatabaseReader createReader(String cityDbPath) {
        if (cityDbPath == null || cityDbPath.isBlank()) {
            LOGGER.info("GeoIP city database is not configured");
            return null;
        }
        Path path = Path.of(cityDbPath.trim());
        if (!Files.isRegularFile(path)) {
            LOGGER.warn("GeoIP city database was not found at {}", path);
            return null;
        }
        try {
            return new DatabaseReader.Builder(new File(path.toString())).build();
        } catch (IOException ex) {
            LOGGER.warn("Could not open GeoIP city database at {}", path, ex);
            return null;
        }
    }

    private boolean isPrivateAddress(InetAddress address) {
        return address.isAnyLocalAddress()
                || address.isLoopbackAddress()
                || address.isLinkLocalAddress()
                || address.isSiteLocalAddress()
                || address.isMulticastAddress();
    }

    private String name(NamedRecord record) {
        if (record == null || record.names() == null || record.names().isEmpty()) {
            return null;
        }
        Map<String, String> names = record.names();
        String englishName = names.get("en");
        if (englishName != null && !englishName.isBlank()) {
            return englishName;
        }
        return names.values().stream()
                .filter(value -> value != null && !value.isBlank())
                .findFirst()
                .orElse(null);
    }

    public record GeoLocation(
            String countryCode,
            String countryName,
            String region,
            String city,
            Double latitude,
            Double longitude,
            String timezone
    ) {
        public static GeoLocation empty() {
            return new GeoLocation(null, null, null, null, null, null, null);
        }
    }
}
