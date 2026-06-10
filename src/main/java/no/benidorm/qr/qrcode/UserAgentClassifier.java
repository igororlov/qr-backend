package no.benidorm.qr.qrcode;

final class UserAgentClassifier {
    private UserAgentClassifier() {
    }

    static String deviceType(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return "Unknown";
        }
        String value = userAgent.toLowerCase();
        if (value.contains("bot") || value.contains("crawler") || value.contains("spider")) {
            return "Bot";
        }
        if (value.contains("ipad") || value.contains("tablet")) {
            return "Tablet";
        }
        if (value.contains("mobile") || value.contains("iphone") || value.contains("android")) {
            return "Mobile";
        }
        if (value.contains("macintosh") || value.contains("windows") || value.contains("linux")) {
            return "Desktop";
        }
        return "Unknown";
    }
}
