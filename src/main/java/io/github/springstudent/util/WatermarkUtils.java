package io.github.springstudent.util;

import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.util.Matrix;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

/**
 * @author zhouning
 * @date 2024/03/26 18:16
 */
public class WatermarkUtils {

    public static void addTextWatermark(
            PDPageContentStream contentStream,
            String watermarkText,
            PDDocument document,
            PDPage page,
            float rotation,
            int widthSpacer,
            int heightSpacer,
            float fontSize,
            String alphabet)
            throws IOException {
        String resourceDir = "";
        PDFont font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        switch (alphabet) {
            case "arabic":
                resourceDir = "static/fonts/NotoSansArabic-Regular.ttf";
                break;
            case "japanese":
                resourceDir = "static/fonts/Meiryo.ttf";
                break;
            case "korean":
                resourceDir = "static/fonts/malgun.ttf";
                break;
            case "chinese":
                resourceDir = "static/fonts/SimSun.ttf";
                break;
            case "roman":
            default:
                resourceDir = "static/fonts/NotoSans-Regular.ttf";
                break;
        }

        if (!"".equals(resourceDir)) {
            ClassPathResource classPathResource = new ClassPathResource(resourceDir);
            String fileExtension = resourceDir.substring(resourceDir.lastIndexOf("."));
            File tempFile = Files.createTempFile("NotoSansFont", fileExtension).toFile();
            try (InputStream is = classPathResource.getInputStream();
                 FileOutputStream os = new FileOutputStream(tempFile)) {
                IOUtils.copy(is, os);
            }

            font = PDType0Font.load(document, tempFile);
            tempFile.deleteOnExit();
        }

        contentStream.setFont(font, fontSize);
        contentStream.setNonStrokingColor(Color.LIGHT_GRAY);

        String[] textLines = watermarkText.split("\\\\n");
        float maxLineWidth = 0;

        for (int i = 0; i < textLines.length; ++i) {
            maxLineWidth = Math.max(maxLineWidth, font.getStringWidth(textLines[i]));
        }

        // Set size and location of text watermark
        float watermarkWidth = widthSpacer + maxLineWidth * fontSize / 1000;
        float watermarkHeight = heightSpacer + fontSize * textLines.length;
        float pageWidth = page.getMediaBox().getWidth();
        float pageHeight = page.getMediaBox().getHeight();
        int watermarkRows = (int) (pageHeight / watermarkHeight + 1);
        int watermarkCols = (int) (pageWidth / watermarkWidth + 1);

        // Add the text watermark
        for (int i = 0; i < watermarkRows; i++) {
            for (int j = 0; j < watermarkCols; j++) {
                contentStream.beginText();
                contentStream.setTextMatrix(
                        Matrix.getRotateInstance(
                                (float) Math.toRadians(rotation),
                                j * watermarkWidth,
                                i * watermarkHeight));

                for (int k = 0; k < textLines.length; ++k) {
                    contentStream.showText(textLines[k]);
                    contentStream.newLineAtOffset(0, -fontSize);
                }

                contentStream.endText();
            }
        }
    }

    public static void addImageWatermark(
            PDPageContentStream contentStream,
            MultipartFile watermarkImage,
            PDDocument document,
            PDPage page,
            float rotation,
            int widthSpacer,
            int heightSpacer,
            float fontSize)
            throws IOException {

        // Load the watermark image
        BufferedImage image = ImageIO.read(watermarkImage.getInputStream());

        // Compute width based on original aspect ratio
        float aspectRatio = (float) image.getWidth() / (float) image.getHeight();

        // Desired physical height (in PDF points)
        float desiredPhysicalHeight = fontSize;

        // Desired physical width based on the aspect ratio
        float desiredPhysicalWidth = desiredPhysicalHeight * aspectRatio;

        // Convert the BufferedImage to PDImageXObject
        PDImageXObject xobject = LosslessFactory.createFromImage(document, image);

        // Calculate the number of rows and columns for watermarks
        float pageWidth = page.getMediaBox().getWidth();
        float pageHeight = page.getMediaBox().getHeight();
        int watermarkRows =
                (int) ((pageHeight + heightSpacer) / (desiredPhysicalHeight + heightSpacer));
        int watermarkCols =
                (int) ((pageWidth + widthSpacer) / (desiredPhysicalWidth + widthSpacer));

        for (int i = 0; i < watermarkRows; i++) {
            for (int j = 0; j < watermarkCols; j++) {
                float x = j * (desiredPhysicalWidth + widthSpacer);
                float y = i * (desiredPhysicalHeight + heightSpacer);

                // Save the graphics state
                contentStream.saveGraphicsState();

                // Create rotation matrix and rotate
                contentStream.transform(
                        Matrix.getTranslateInstance(
                                x + desiredPhysicalWidth / 2, y + desiredPhysicalHeight / 2));
                contentStream.transform(Matrix.getRotateInstance(Math.toRadians(rotation), 0, 0));
                contentStream.transform(
                        Matrix.getTranslateInstance(
                                -desiredPhysicalWidth / 2, -desiredPhysicalHeight / 2));

                // Draw the image and restore the graphics state
                contentStream.drawImage(xobject, 0, 0, desiredPhysicalWidth, desiredPhysicalHeight);
                contentStream.restoreGraphicsState();
            }
        }
    }
}
