package io.github.springstudent.util;


import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;

/**
 * @author zhouning
 * @date 2023/08/15 10:05
 */
public class ImageUtil {

    public static byte[] createImage(int width, int height, int MB, String imageType,String bgColor) throws IOException {
        return doCreateImage(width, height, MB * 1024 * 1024, imageType, MB + "MB",bgColor);
    }

    private static byte[] doCreateImage(int width, int height, int size, String imageType, String text,String bgColor) throws IOException {
        // 创建BufferedImage
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(new Color(Integer.parseInt(bgColor.substring(0, 2), 16),Integer.parseInt(bgColor.substring(2, 4), 16), Integer.parseInt(bgColor.substring(4, 6), 16)));
        g2d.fillRect(0, 0, image.getWidth(), image.getHeight());
        g2d.setColor(Color.BLACK);
        //设置字体大小，并让位置居中
        int fontSize;
        int mb = (size / (1024 * 1024));
        if (mb >= 40) {
            fontSize = 320;
        } else {
            fontSize = mb * 8;
        }
        g2d.setFont(new Font(null, Font.PLAIN, fontSize));
        FontMetrics metrics = g2d.getFontMetrics();
        int x = (width - metrics.stringWidth(text)) / 2;
        int y = ((height - metrics.getHeight()) / 2) + metrics.getAscent();
        g2d.drawString(text, x, y);
        g2d.dispose();
        //填充文件到指定大小
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, imageType, outputStream);
        byte[] imageBytes = outputStream.toByteArray();
        int currentSize = imageBytes.length;
        while (currentSize < size) {
            // 图片进行填充
            Random random = new Random();
            byte[] tmpBytes = new byte[imageBytes.length * 2];
            if (tmpBytes.length > currentSize) {
                tmpBytes = new byte[size];
            }
            random.nextBytes(tmpBytes);
            System.arraycopy(imageBytes, 0, tmpBytes, 0, imageBytes.length);
            imageBytes = tmpBytes;
            currentSize = imageBytes.length;
        }
        return imageBytes;
    }

}
