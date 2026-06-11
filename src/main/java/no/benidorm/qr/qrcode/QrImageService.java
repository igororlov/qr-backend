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
    private static final int LABEL_HEIGHT = 86;
    private static final int SIDE_LABEL_WIDTH = 86;
    private static final int LABEL_FONT_SIZE = 74;
    private static final int LABEL_GAP = 2;
    private static final int QUIET_ZONE = 4;
    private static final int LOGO_BOX_SIZE = 210;
    private static final Duration LOGO_READ_TIMEOUT = Duration.ofSeconds(5);

    public GeneratedQrImage generate(QrCode qrCode, String url) {
        LogoAsset logo = qrCode.isQrLogoEnabled() ? loadLogo(qrCode) : null;
        return generate(
                url,
                new QrImageLabels(qrCode.getLabelTop(), qrCode.getLabelLeft(), qrCode.getLabelRight(), qrCode.getLabel()),
                qrCode.getQrForegroundColor(),
                qrCode.getQrBackgroundColor(),
                qrCode.isQrBackgroundTransparent(),
                logo
        );
    }

    public GeneratedQrImage generate(String url, String label) {
        return generate(url, new QrImageLabels(null, null, null, label), "#111111", "#ffffff", false, null);
    }

    private GeneratedQrImage generate(
            String url,
            QrImageLabels labels,
            String foregroundHex,
            String backgroundHex,
            boolean backgroundTransparent,
            LogoAsset logo
    ) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(url, BarcodeFormat.QR_CODE, SIZE, SIZE, Map.of(
                    EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H,
                    EncodeHintType.MARGIN, QUIET_ZONE
            ));
            Color foreground = Color.decode(foregroundHex);
            Color background = Color.decode(backgroundHex);
            int topHeight = hasText(labels.top()) ? LABEL_HEIGHT : 0;
            int leftWidth = hasText(labels.left()) ? SIDE_LABEL_WIDTH : 0;
            int rightWidth = hasText(labels.right()) ? SIDE_LABEL_WIDTH : 0;
            int width = leftWidth + SIZE + rightWidth;
            int height = topHeight + SIZE + LABEL_HEIGHT;
            int qrX = leftWidth;
            int qrY = topHeight;
            BufferedImage image = new BufferedImage(
                    width,
                    height,
                    backgroundTransparent ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB
            );
            Graphics2D graphics = image.createGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (!backgroundTransparent) {
                graphics.setColor(background);
                graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
            }
            drawMatrix(graphics, matrix, foreground, qrX, qrY);
            drawLogo(graphics, logo == null ? null : logo.image(), background, qrX, qrY);
            drawLabels(graphics, labels, foreground, width, topHeight, leftWidth, qrX, qrY);
            graphics.dispose();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(image, "png", out);
            return new GeneratedQrImage(
                    out.toByteArray(),
                    svg(matrix, labels, foregroundHex, backgroundHex, backgroundTransparent, logo)
            );
        } catch (IllegalArgumentException | WriterException | IOException ex) {
            throw new BadRequestException("Could not generate QR image");
        }
    }

    private String svg(
            BitMatrix matrix,
            QrImageLabels labels,
            String foregroundHex,
            String backgroundHex,
            boolean backgroundTransparent,
            LogoAsset logo
    ) {
        int topHeight = hasText(labels.top()) ? LABEL_HEIGHT : 0;
        int leftWidth = hasText(labels.left()) ? SIDE_LABEL_WIDTH : 0;
        int rightWidth = hasText(labels.right()) ? SIDE_LABEL_WIDTH : 0;
        int width = leftWidth + SIZE + rightWidth;
        int height = topHeight + SIZE + LABEL_HEIGHT;
        int qrX = leftWidth;
        int qrY = topHeight;
        StringBuilder svg = new StringBuilder();
        svg.append("<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 ")
                .append(width)
                .append(' ')
                .append(height)
                .append("\" width=\"")
                .append(width)
                .append("\" height=\"")
                .append(height)
                .append("\" fill=\"none\">");
        if (!backgroundTransparent) {
            svg.append("<rect width=\"100%\" height=\"100%\" fill=\"").append(escape(foregroundOrBackground(backgroundHex))).append("\"/>");
        }
        svg.append("<g fill=\"").append(escape(foregroundOrBackground(foregroundHex))).append("\">");
        for (int y = 0; y < matrix.getHeight(); y++) {
            for (int x = 0; x < matrix.getWidth(); x++) {
                if (matrix.get(x, y)) {
                    svg.append("<rect x=\"").append(x + qrX).append("\" y=\"").append(y + qrY).append("\" width=\"1\" height=\"1\"/>");
                }
            }
        }
        svg.append("</g>");
        appendSvgLogo(svg, logo, backgroundHex, qrX, qrY);
        appendSvgLabels(svg, labels, foregroundHex, width, topHeight, leftWidth, qrX, qrY);
        svg.append("</svg>");
        return svg.toString();
    }

    private void drawMatrix(Graphics2D graphics, BitMatrix matrix, Color foreground, int qrX, int qrY) {
        graphics.setColor(foreground);
        for (int y = 0; y < matrix.getHeight(); y++) {
            for (int x = 0; x < matrix.getWidth(); x++) {
                if (matrix.get(x, y)) {
                    graphics.fillRect(x + qrX, y + qrY, 1, 1);
                }
            }
        }
    }

    private void drawLogo(Graphics2D graphics, BufferedImage logo, Color background, int qrX, int qrY) {
        if (logo == null) {
            return;
        }

        int boxX = qrX + (SIZE - LOGO_BOX_SIZE) / 2;
        int boxY = qrY + (SIZE - LOGO_BOX_SIZE) / 2;
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

    private void drawLabels(Graphics2D graphics, QrImageLabels labels, Color foreground, int width, int topHeight, int leftWidth, int qrX, int qrY) {
        graphics.setColor(foreground);
        graphics.setFont(new Font("Roboto", Font.BOLD, LABEL_FONT_SIZE));
        FontMetrics metrics = graphics.getFontMetrics();
        drawHorizontalLabel(graphics, labels.top(), width, qrY - LABEL_GAP - metrics.getDescent());
        drawHorizontalLabel(graphics, labels.bottom(), width, qrY + SIZE + LABEL_GAP + metrics.getAscent());
        drawVerticalLabel(graphics, labels.left(), qrY, leftWidth / 2, true);
        drawVerticalLabel(graphics, labels.right(), qrY, qrX + SIZE + SIDE_LABEL_WIDTH / 2, false);
    }

    private void drawHorizontalLabel(Graphics2D graphics, String label, int width, int baselineY) {
        if (!hasText(label)) {
            return;
        }
        FontMetrics metrics = graphics.getFontMetrics();
        String text = truncateLabel(label);
        int x = Math.max(24, (width - metrics.stringWidth(text)) / 2);
        graphics.drawString(text, x, baselineY);
    }

    private void drawVerticalLabel(Graphics2D graphics, String label, int qrY, int centerX, boolean leftSide) {
        if (!hasText(label)) {
            return;
        }
        Graphics2D copy = (Graphics2D) graphics.create();
        copy.setFont(graphics.getFont());
        FontMetrics metrics = copy.getFontMetrics();
        String text = truncateLabel(label);
        int textWidth = metrics.stringWidth(text);
        if (leftSide) {
            copy.translate(centerX + metrics.getAscent() / 2, qrY + (SIZE + textWidth) / 2);
            copy.rotate(-Math.PI / 2);
        } else {
            copy.translate(centerX - metrics.getAscent() / 2, qrY + (SIZE - textWidth) / 2);
            copy.rotate(Math.PI / 2);
        }
        copy.drawString(text, 0, 0);
        copy.dispose();
    }

    private void appendSvgLogo(StringBuilder svg, LogoAsset logo, String backgroundHex, int qrX, int qrY) {
        if (logo == null) {
            return;
        }
        int boxX = qrX + (SIZE - LOGO_BOX_SIZE) / 2;
        int boxY = qrY + (SIZE - LOGO_BOX_SIZE) / 2;
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

    private void appendSvgLabels(StringBuilder svg, QrImageLabels labels, String foregroundHex, int width, int topHeight, int leftWidth, int qrX, int qrY) {
        appendSvgHorizontalLabel(svg, labels.top(), width / 2, qrY - LABEL_GAP, "text-after-edge", foregroundHex);
        appendSvgHorizontalLabel(svg, labels.bottom(), width / 2, qrY + SIZE + LABEL_GAP, "text-before-edge", foregroundHex);
        appendSvgVerticalLabel(svg, labels.left(), leftWidth / 2, qrY + SIZE / 2, -90, foregroundHex);
        appendSvgVerticalLabel(svg, labels.right(), qrX + SIZE + SIDE_LABEL_WIDTH / 2, qrY + SIZE / 2, 90, foregroundHex);
    }

    private void appendSvgHorizontalLabel(StringBuilder svg, String label, int x, int y, String dominantBaseline, String foregroundHex) {
        if (!hasText(label)) {
            return;
        }
        String text = truncateLabel(label);
        svg.append("<text x=\"").append(x).append("\" y=\"").append(y)
                .append("\" text-anchor=\"middle\" dominant-baseline=\"")
                .append(dominantBaseline)
                .append("\" font-family=\"Roboto, Arial, Helvetica, sans-serif\" font-size=\"")
                .append(LABEL_FONT_SIZE)
                .append("\" font-weight=\"700\" fill=\"")
                .append(escape(foregroundOrBackground(foregroundHex))).append("\">")
                .append(escape(text))
                .append("</text>");
    }

    private void appendSvgVerticalLabel(StringBuilder svg, String label, int x, int y, int angle, String foregroundHex) {
        if (!hasText(label)) {
            return;
        }
        String text = truncateLabel(label);
        svg.append("<text x=\"").append(x).append("\" y=\"").append(y)
                .append("\" text-anchor=\"middle\" dominant-baseline=\"central\" transform=\"rotate(")
                .append(angle).append(' ').append(x).append(' ').append(y)
                .append(")\" font-family=\"Roboto, Arial, Helvetica, sans-serif\" font-size=\"")
                .append(LABEL_FONT_SIZE)
                .append("\" font-weight=\"700\" fill=\"")
                .append(escape(foregroundOrBackground(foregroundHex))).append("\">")
                .append(escape(text))
                .append("</text>");
    }

    private LogoAsset loadLogo(QrCode qrCode) {
        if (qrCode.getLogoBytes() != null) {
            return logoAsset(qrCode.getLogoBytes(), qrCode.getLogoContentType());
        }
        if (qrCode.getCompany().getLogoBytes() != null) {
            return logoAsset(qrCode.getCompany().getLogoBytes(), qrCode.getCompany().getLogoContentType());
        }

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

    private LogoAsset logoAsset(byte[] bytes, String contentType) {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
            if (image == null) {
                return null;
            }
            String normalizedContentType = contentType;
            if (normalizedContentType == null || normalizedContentType.isBlank()) {
                normalizedContentType = URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(bytes));
            }
            if (normalizedContentType == null || normalizedContentType.isBlank()) {
                normalizedContentType = "image/png";
            }
            String dataUri = "data:" + normalizedContentType + ";base64," + Base64.getEncoder().encodeToString(bytes);
            return new LogoAsset(image, dataUri);
        } catch (IOException ex) {
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

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String truncateLabel(String value) {
        String trimmed = value.trim();
        return trimmed.length() > 36 ? trimmed.substring(0, 36) : trimmed;
    }

    public record GeneratedQrImage(byte[] png, String svg) {
        public byte[] svgBytes() {
            return svg.getBytes(StandardCharsets.UTF_8);
        }
    }

    private record LogoAsset(BufferedImage image, String dataUri) {
    }

    private record QrImageLabels(String top, String left, String right, String bottom) {
    }
}
