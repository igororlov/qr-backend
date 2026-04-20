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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URLConnection;
import java.time.Duration;
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

    public byte[] png(QrCode qrCode, String url) {
        BufferedImage logo = qrCode.isQrLogoEnabled() ? loadLogo(qrCode) : null;
        return png(
                url,
                qrCode.getLabel(),
                qrCode.getQrForegroundColor(),
                qrCode.getQrBackgroundColor(),
                logo
        );
    }

    public byte[] png(String url, String label) {
        return png(url, label, "#111111", "#ffffff", null);
    }

    private byte[] png(String url, String label, String foregroundHex, String backgroundHex, BufferedImage logo) {
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
            drawLogo(graphics, logo, background);
            drawLabel(graphics, label, foreground);
            graphics.dispose();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(image, "png", out);
            return out.toByteArray();
        } catch (IllegalArgumentException | WriterException | IOException ex) {
            throw new BadRequestException("Could not generate QR image");
        }
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

    private BufferedImage loadLogo(QrCode qrCode) {
        String logoUrl = firstPresent(qrCode.getLogoUrl(), qrCode.getCompany().getLogoUrl());
        if (logoUrl == null) {
            return null;
        }
        try {
            URLConnection connection = URI.create(logoUrl).toURL().openConnection();
            connection.setConnectTimeout((int) LOGO_READ_TIMEOUT.toMillis());
            connection.setReadTimeout((int) LOGO_READ_TIMEOUT.toMillis());
            return ImageIO.read(connection.getInputStream());
        } catch (IllegalArgumentException | IOException ex) {
            return null;
        }
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
}
