package no.benidorm.qr.qrcode;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import no.benidorm.qr.common.BadRequestException;
import org.springframework.stereotype.Service;

@Service
public class QrImageService {
    private static final int SIZE = 900;
    private static final int LABEL_HEIGHT = 120;

    public byte[] png(String url, String label) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(url, BarcodeFormat.QR_CODE, SIZE, SIZE);
            BufferedImage qr = MatrixToImageWriter.toBufferedImage(matrix);
            BufferedImage image = new BufferedImage(SIZE, SIZE + LABEL_HEIGHT, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = image.createGraphics();
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
            graphics.drawImage(qr, 0, 0, null);
            drawLabel(graphics, label);
            graphics.dispose();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(image, "png", out);
            return out.toByteArray();
        } catch (WriterException | IOException ex) {
            throw new BadRequestException("Could not generate QR image");
        }
    }

    private void drawLabel(Graphics2D graphics, String label) {
        if (label == null || label.isBlank()) {
            return;
        }
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setColor(Color.BLACK);
        graphics.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 42));
        FontMetrics metrics = graphics.getFontMetrics();
        String text = label.length() > 36 ? label.substring(0, 36) : label;
        int x = Math.max(24, (SIZE - metrics.stringWidth(text)) / 2);
        int y = SIZE + (LABEL_HEIGHT + metrics.getAscent()) / 2 - 12;
        graphics.drawString(text, x, y);
    }
}

