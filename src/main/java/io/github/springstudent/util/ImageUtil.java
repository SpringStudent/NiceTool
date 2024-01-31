package io.github.springstudent.util;


import cn.hutool.core.util.IdUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author zhouning
 * @date 2023/08/15 10:05
 */
public class ImageUtil {

    public static byte[] createImage(int width, int height, int MB, String imageType, String bgColor) throws IOException {
        return doCreateImage(width, height, MB * 1024 * 1024, imageType, MB + "MB", bgColor);
    }

    public static byte[] createBatchImage(int num, int width, int height, int MB, String imageType, String bgColor) throws IOException {
        List<byte[]> bytesList = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            bytesList.add(doCreateImage(width, height, MB * 1024 * 1024, imageType, MB + "MB", bgColor));
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);
        for (byte[] file : bytesList) {
            // 创建一个新的zip条目，并将其添加到zip输出流中
            ZipEntry entry = new ZipEntry(IdUtil.fastSimpleUUID() + "." + imageType);
            zos.putNextEntry(entry);
            // 将文件的字节数组写入当前zip条目
            ByteArrayInputStream bais = new ByteArrayInputStream(file);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = bais.read(buffer)) > 0) {
                zos.write(buffer, 0, len);
            }
            // 关闭当前zip条目
            zos.closeEntry();
        }
        // 关闭zip输出流和字节数组输出流
        zos.close();
        baos.close();
        return baos.toByteArray();
    }

    private static byte[] doCreateImage(int width, int height, int size, String imageType, String text, String bgColor) throws IOException {
        // 创建BufferedImage
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(new Color(Integer.parseInt(bgColor.substring(0, 2), 16), Integer.parseInt(bgColor.substring(2, 4), 16), Integer.parseInt(bgColor.substring(4, 6), 16)));
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
