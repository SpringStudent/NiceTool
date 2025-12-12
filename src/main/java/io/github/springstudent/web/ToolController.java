package io.github.springstudent.web;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import io.github.springstudent.autocode.domain.Column;
import io.github.springstudent.autocode.domain.SqlInfo;
import io.github.springstudent.autocode.util.SqlAnaly;
import io.github.springstudent.bean.AddWatermarkRequest;
import io.github.springstudent.bean.InterfaceLimit;
import io.github.springstudent.bean.ResponseEntity;
import io.github.springstudent.util.ImageUtil;
import io.github.springstudent.util.Ip2RegionUtil;
import io.github.springstudent.util.WatermarkUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author 周宁
 */
@RestController
@RequestMapping("/tool")
public class ToolController {

    @Resource
    private HttpServletResponse response;


    @GetMapping("/createImage")
    @InterfaceLimit
    public void createImage(@RequestParam int width, @RequestParam int height, @RequestParam int MB, @RequestParam String imageType, @RequestParam String imageColor) throws Exception {
        try {
            if (MB > 200) {
                response.setCharacterEncoding("UTF-8");
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().println(JSONUtil.toJsonStr(ResponseEntity.fail("图片大小不能超过200MB", 500)));
                return;
            }
            imageType = imageType.toLowerCase();
            byte[] bytes = ImageUtil.createImage(width, height, MB, imageType, imageColor);
            OutputStream outputStream = response.getOutputStream();
            response.addHeader("Access-Control-Expose-Headers", "Content-Disposition");
            response.addHeader("Content-Disposition", "attachment;filename=" + IdUtil.fastSimpleUUID() + "." + imageType);
            response.addHeader("Content-Type", "image/" + imageType);
            outputStream.write(bytes);
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(JSONUtil.toJsonStr(ResponseEntity.fail("创建图片失败", 500)));

        }
    }

    @GetMapping("/createBatchImage")
    @InterfaceLimit(time = 3000, value = 1)
    public void createBatchImage(@RequestParam int imageNum, @RequestParam int width, @RequestParam int height, @RequestParam int MB, @RequestParam String imageType, @RequestParam String imageColor) throws Exception {
        try {
            if (imageNum * MB > 200) {
                response.setCharacterEncoding("UTF-8");
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().println(JSONUtil.toJsonStr(ResponseEntity.fail("多个图片的总大小不能超过200MB", 500)));
                return;
            }
            imageType = imageType.toLowerCase();
            byte[] bytes = ImageUtil.createBatchImage(imageNum, width, height, MB, imageType, imageColor);
            OutputStream outputStream = response.getOutputStream();
            response.addHeader("Access-Control-Expose-Headers", "Content-Disposition");
            response.addHeader("Content-Disposition", "attachment;filename=" + IdUtil.fastSimpleUUID() + ".zip");
            response.addHeader("Content-Type", "application/zip");
            outputStream.write(bytes);
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(JSONUtil.toJsonStr(ResponseEntity.fail("批量创建图片失败", 500)));
            return;
        }
    }


    @GetMapping("/ipRegion")
    @InterfaceLimit
    public ResponseEntity<String> ipRegion(@RequestParam String ip) throws Exception {
        return ResponseEntity.success(Ip2RegionUtil.ip2Region(ip));
    }

    @PostMapping(consumes = "multipart/form-data", value = "/addWatermark")
    @InterfaceLimit
    public org.springframework.http.ResponseEntity<byte[]> addWatermark(@ModelAttribute AddWatermarkRequest request) throws IOException, Exception {
        MultipartFile pdfFile = request.getFileInput();
        String watermarkType = request.getWatermarkType();
        String watermarkText = request.getWatermarkText();
        MultipartFile watermarkImage = request.getWatermarkImage();
        String alphabet = request.getAlphabet();
        float fontSize = request.getFontSize();
        float rotation = request.getRotation();
        float opacity = request.getOpacity();
        int widthSpacer = request.getWidthSpacer();
        int heightSpacer = request.getHeightSpacer();
        PDDocument document = Loader.loadPDF(pdfFile.getBytes());
        for (PDPage page : document.getPages()) {
            PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true);
            PDExtendedGraphicsState graphicsState = new PDExtendedGraphicsState();
            graphicsState.setNonStrokingAlphaConstant(opacity);
            contentStream.setGraphicsStateParameters(graphicsState);
            if ("text".equalsIgnoreCase(watermarkType)) {
                WatermarkUtils.addTextWatermark(contentStream, watermarkText, document, page, rotation, widthSpacer, heightSpacer, fontSize, alphabet);
            } else if ("image".equalsIgnoreCase(watermarkType)) {
                WatermarkUtils.addImageWatermark(contentStream, watermarkImage, document, page, rotation, widthSpacer, heightSpacer, fontSize);
            }
            contentStream.close();
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        document.save(baos);
        document.close();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentLength(baos.toByteArray().length);
        String encodedDocName = URLEncoder.encode(pdfFile.getOriginalFilename().replaceFirst("[.][^.]+$", "") + "_watermarked.pdf", StandardCharsets.UTF_8.toString()).replaceAll("\\+", "%20");
        headers.setContentDispositionFormData("attachment", encodedDocName);
        return new org.springframework.http.ResponseEntity<>(baos.toByteArray(), headers, HttpStatus.OK);
    }

    @GetMapping("/sqlToPojo")
    public ResponseEntity<List<Column>> sqlToPojo(@RequestParam String sql) {
        try {
            SqlInfo sqlInfo = SqlAnaly.analy(sql);
            if (!sqlInfo.isValid()) {
                return ResponseEntity.fail("无法解析出有效的表结构信息，请检查SQL语句的正确性", 500);
            } else {
                return ResponseEntity.success(sqlInfo.getColumnList());
            }
        } catch (Exception e) {
            return ResponseEntity.fail("SQL解析失败：" + e.getMessage(), 500);
        }
    }
}
