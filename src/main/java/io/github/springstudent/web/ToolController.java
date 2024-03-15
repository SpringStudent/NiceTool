package io.github.springstudent.web;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import io.github.springstudent.bean.InterfaceLimit;
import io.github.springstudent.bean.ResponseEntity;
import io.github.springstudent.util.ImageUtil;
import io.github.springstudent.util.Ip2RegionUtil;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;

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
}
