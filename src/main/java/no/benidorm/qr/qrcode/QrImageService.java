package no.benidorm.qr.qrcode;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import javax.imageio.ImageIO;
import no.benidorm.qr.common.BadRequestException;
import org.springframework.stereotype.Service;

@Service
public class QrImageService {
    private static final int SIZE = 900;
    private static final int LABEL_HEIGHT = 120;
    private static final int QUIET_ZONE = 4;
    private static final int LOGO_BOX_SIZE = 210;
    private static final Duration LOGO_READ_TIMEOUT = Duration.ofSeconds(5);

    public GeneratedQrImage generate(QrCode qrCode, String url) {
        LogoAsset logo = qrCode.isQrLogoEnabled() ? loadLogo(qrCode) : null;
        return generate(
                url,
                qrCode.getLabel(),
                qrCode.getQrForegroundColor(),
                qrCode.getQrBackgroundColor(),
                logo
        );
    }

    public GeneratedQrImage generate(String url, String label) {
        return generate(url, label, "#111111", "#ffffff", null);
    }

    private GeneratedQrImage generate(String url, String label, String foregroundHex, String backgroundHex, LogoAsset logo) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(url, BarcodeFormat.QR_CODE, SIZE, SIZE, Map.of(
                    EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H,
                    EncodeHintType.MARGIN, QUIET_ZONE
            ));
            Color foreground = Color.decode(foregroundHex);
            Color background = Color.decode(backgroundHex);
            BufferedImage image = new BufferedImage(SIZE, SIZE + LABEL_HEIGHT, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = image.createGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setColor(background);
            graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
            drawMatrix(graphics, matrix, foreground);
            drawLogo(graphics, logo == null ? null : logo.image(), background);
            drawLabel(graphics, label, foreground);
            graphics.dispose();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(image, "png", out);
            return new GeneratedQrImage(
                    out.toByteArray(),
                    svg(matrix, label, foregroundHex, backgroundHex, logo)
            );
        } catch (IllegalArgumentException | WriterException | IOException ex) {
            throw new BadRequestException("Could not generate QR image");
        }
    }

    private String svg(BitMatrix matrix, String label, String foregroundHex, String backgroundHex, LogoAsset logo) {
        StringBuilder svg = new StringBuilder();
        svg.append("<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 ")
                .append(SIZE)
                .append(' ')
                .append(SIZE + LABEL_HEIGHT)
                .append("\" width=\"")
                .append(SIZE)
                .append("\" height=\"")
                .append(SIZE + LABEL_HEIGHT)
                .append("\" fill=\"none\">");
        svg.append("<rect width=\"100%\" height=\"100%\" fill=\"").append(escape(foregroundOrBackground(backgroundHex))).append("\"/>");
        svg.append("<g fill=\"").append(escape(foregroundOrBackground(foregroundHex))).append("\">");
        for (int y = 0; y < matrix.getHeight(); y++) {
            for (int x = 0; x < matrix.getWidth(); x++) {
                if (matrix.get(x, y)) {
                    svg.append("<rect x=\"").append(x).append("\" y=\"").append(y).append("\" width=\"1\" height=\"1\"/>");
                }
            }
        }
        svg.append("</g>");
        appendSvgLogo(svg, logo, backgroundHex);
        appendSvgLabel(svg, label, foregroundHex);
        svg.append("</svg>");
        return svg.toString();
    }

    private void drawMatrix(Graphics2D graphics, BitMatrix matrix, Color foreground) {
        graphics.setColor(foreground);
        for (int y = 0; y < matrix.getHeight(); y++) {
            for (int x = 0; x < matrix.getWidth(); x++) {
                if (matrix.get(x, y)) {
                    graphics.fillRect(x, y, 1, 1);
                }
            }
        }
    }

    private void drawLogo(Graphics2D graphics, BufferedImage logo, Color background) {
        if (logo == null) {
            return;
        }

        int boxX = (SIZE - LOGO_BOX_SIZE) / 2;
        int boxY = (SIZE - LOGO_BOX_SIZE) / 2;
        int arc = 36;
        RoundRectangle2D.Float box = new RoundRectangle2D.Float(boxX, boxY, LOGO_BOX_SIZE, LOGO_BOX_SIZE, arc, arc);
        graphics.setColor(background);
        graphics.fill(box);
        graphics.setColor(Color.WHITE);
        graphics.setStroke(new BasicStroke(12));
        graphics.draw(box);

        int maxLogo = LOGO_BOX_SIZE - 52;
        double scale = Math.min((double) maxLogo / logo.getWidth(), (double) maxLogo / logo.getHeight());
        int width = Math.max(1, (int) Math.round(logo.getWidth() * scale));
        int height = Math.max(1, (int) Math.round(logo.getHeight() * scale));
        int x = boxX + (LOGO_BOX_SIZE - width) / 2;
        int y = boxY + (LOGO_BOX_SIZE - height) / 2;
        graphics.drawImage(logo, x, y, width, height, null);
    }

    private void drawLabel(Graphics2D graphics, String label, Color foreground) {
        if (label == null || label.isBlank()) {
            return;
        }
        graphics.setColor(foreground);
        graphics.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 42));
        FontMetrics metrics = graphics.getFontMetrics();
        String text = label.length() > 36 ? label.substring(0, 36) : label;
        int x = Math.max(24, (SIZE - metrics.stringWidth(text)) / 2);
        int y = SIZE + (LABEL_HEIGHT + metrics.getAscent()) / 2 - 12;
        graphics.drawString(text, x, y);
    }

    private void appendSvgLogo(StringBuilder svg, LogoAsset logo, String backgroundHex) {
        if (logo == null) {
            return;
        }
        int boxX = (SIZE - LOGO_BOX_SIZE) / 2;
        int boxY = (SIZE - LOGO_BOX_SIZE) / 2;
        int arc = 36;
        int maxLogo = LOGO_BOX_SIZE - 52;
        double scale = Math.min((double) maxLogo / logo.image().getWidth(), (double) maxLogo / logo.image().getHeight());
        int width = Math.max(1, (int) Math.round(logo.image().getWidth() * scale));
        int height = Math.max(1, (int) Math.round(logo.image().getHeight() * scale));
        int x = boxX + (LOGO_BOX_SIZE - width) / 2;
        int y = boxY + (LOGO_BOX_SIZE - height) / 2;

        svg.append("<rect x=\"").append(boxX).append("\" y=\"").append(boxY)
                .append("\" width=\"").append(LOGO_BOX_SIZE).append("\" height=\"").append(LOGO_BOX_SIZE)
                .append("\" rx=\"").append(arc / 2).append("\" fill=\"").append(escape(foregroundOrBackground(backgroundHex)))
                .append("\" stroke=\"#ffffff\" stroke-width=\"12\"/>");
        svg.append("<image href=\"").append(logo.dataUri()).append("\" x=\"").append(x).append("\" y=\"").append(y)
                .append("\" width=\"").append(width).append("\" height=\"").append(height)
                .append("\" preserveAspectRatio=\"xMidYMid meet\"/>");
    }

    private void appendSvgLabel(StringBuilder svg, String label, String foregroundHex) {
        if (label == null || label.isBlank()) {
            return;
        }
        String text = label.length() > 36 ? label.substring(0, 36) : label;
        svg.append("<text x=\"").append(SIZE / 2).append("\" y=\"").append(SIZE + 74)
                .append("\" text-anchor=\"middle\" font-family=\"Arial, Helvetica, sans-serif\" font-size=\"42\" font-weight=\"700\" fill=\"")
                .append(escape(foregroundOrBackground(foregroundHex))).append("\">")
                .append(escape(text))
                .append("</text>");
    }

    private LogoAsset loadLogo(QrCode qrCode) {
        String logoUrl = firstPresent(qrCode.getLogoUrl(), qrCode.getCompany().getLogoUrl());
        if (logoUrl == null) {
            return null;
        }
        try {
            URLConnection connection = URI.create(logoUrl).toURL().openConnection();
            connection.setConnectTimeout((int) LOGO_READ_TIMEOUT.toMillis());
            connection.setReadTimeout((int) LOGO_READ_TIMEOUT.toMillis());
            byte[] bytes = connection.getInputStream().readAllBytes();
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
            if (image == null) {
                return null;
            }
            String contentType = connection.getContentType();
            if (contentType == null || contentType.isBlank()) {
                contentType = URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(bytes));
            }
            if (contentType == null || contentType.isBlank()) {
                contentType = "image/png";
            }
            String dataUri = "data:" + contentType + ";base64," + Base64.getEncoder().encodeToString(bytes);
            return new LogoAsset(image, dataUri);
        } catch (IllegalArgumentException | IOException ex) {
            return null;
        }
    }

    private String foregroundOrBackground(String hex) {
        return hex == null || hex.isBlank() ? "#111111" : hex;
    }

    private String escape(String value) {
        return value
                .replace("&", "&amp;")
                .replace("\"", "&quot;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private String firstPresent(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        if (second != null && !second.isBlank()) {
            return second;
        }
        return null;
    }

    public record GeneratedQrImage(byte[] png, String svg) {
        public byte[] svgBytes() {
            return svg.getBytes(StandardCharsets.UTF_8);
        }
    }

    private record LogoAsset(BufferedImage image, String dataUri) {
    }
}
