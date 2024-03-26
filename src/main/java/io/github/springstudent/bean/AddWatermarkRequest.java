package io.github.springstudent.bean;

import org.springframework.web.multipart.MultipartFile;
import lombok.Data;
import lombok.EqualsAndHashCode;
@Data
@EqualsAndHashCode(callSuper = true)
public class AddWatermarkRequest extends PDFFile {


    /**
     * 水印类型可以是text、image
     */
    private String watermarkType = "text";

    /**
     * text文本内容
     */
    private String watermarkText;

    /**
     * 图片水印
     */
    private MultipartFile watermarkImage;

    /**
     * 字母表:roman, arabic, japanese, korean, chinese
     */
    private String alphabet = "chinese";

    /**
     * 字体大小
     */
    private float fontSize = 30;
    /**
     * 旋转（0-360）
     */
    private float rotation = 0;
    /**
     * 透明度（0% - 100%）：
     */
    private float opacity = 0.5f;
    /**
     * 水平间距（每个水印之间的水平距离）
     */
    private int widthSpacer = 50;
    /**
     * 垂直间距（每个水印之间的垂直距离）
     */
    private int heightSpacer = 50;
}
