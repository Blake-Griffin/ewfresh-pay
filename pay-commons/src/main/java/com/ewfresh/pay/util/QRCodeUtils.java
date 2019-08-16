package com.ewfresh.pay.util;

import com.google.common.collect.Maps;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 * description: 用于生成二维码
 * @author: JiuDongDong
 * date: 2019/5/31.
 */
public class QRCodeUtils {
    private static Logger logger = LoggerFactory.getLogger(QRCodeUtils.class);

    /**
     * Description: 生成二维码
     * @author: JiuDongDong
     * @param content 待生成二维码的内容
     * @param stream 输出流
     * date: 2019/5/31 14:41
     */
    public static void generate(String content, OutputStream stream) {
        try {
            MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
            Map hints = Maps.newHashMap();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            BitMatrix bitMatrix = multiFormatWriter.encode(content, BarcodeFormat.QR_CODE, 400, 400, hints);
            MatrixToImageWriter.writeToStream(bitMatrix, "jpg", stream);
        } catch (Exception e) {
            logger.error("Create QRCode occurred error!", e);
        }
    }

    /**
     * Description: 生成二维码
     * @author: JiuDongDong
     * @param content 待生成二维码的内容
     * @param pathname 保存路径
     * date: 2019/5/31 14:52
     */
    public static void generate(String content, String pathname) {
        try {
            MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
            Map hints = Maps.newHashMap();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            BitMatrix bitMatrix = multiFormatWriter.encode(content, BarcodeFormat.QR_CODE, 400, 400, hints);
            MatrixToImageWriter.writeToFile(bitMatrix, "jpg", new File(pathname));
        } catch (Exception e) {
            logger.error("Create QRCode occurred error!", e);
        }
    }
}

class MatrixToImageWriter {
    private static Logger logger = LoggerFactory.getLogger(QRCodeUtils.class);
    private static final int BLACK = 0xFF000000;
    private static final int WHITE = 0xFFFFFFFF;

    private MatrixToImageWriter() {
    }

    static void writeToFile(BitMatrix matrix, String format, File file) throws IOException {
        BufferedImage image = toBufferedImage(matrix);
        if (!ImageIO.write(image, format, file)) {
            throw new IOException("Could not write an image of format " + format + " to " + file);
        }
    }

    public static void writeToStream(BitMatrix matrix, String format, OutputStream stream)
            throws IOException {
        BufferedImage image = toBufferedImage(matrix);
        if (!ImageIO.write(image, format, stream)) {
            throw new IOException("Could not write an image of format " + format);
        }
    }

    private static BufferedImage toBufferedImage(BitMatrix matrix) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, matrix.get(x, y) ? BLACK : WHITE);
            }
        }
        return image;
    }

}
