package io.github.springstudent.web;

import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.json.JSONUtil;
import io.github.springstudent.bean.ResponseEntity;
import io.github.springstudent.util.ImageUtil;
import io.github.springstudent.util.Ip2RegionUtil;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
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
    public void createImage(@RequestParam int width, @RequestParam int height, @RequestParam int MB, @RequestParam String imageType) throws IOException {
        try {
            if (MB > 200) {
                response.setCharacterEncoding("UTF-8");
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().println(JSONUtil.toJsonStr(ResponseEntity.fail("image size exceed 200MB", 500)));
                return;
            }
            imageType = imageType.toLowerCase();
            byte[] bytes = ImageUtil.createImage(width, height, MB, imageType);
            OutputStream outputStream = response.getOutputStream();
            response.addHeader("Content-Disposition", "attachment;filename=" + IdUtil.fastSimpleUUID() + "." + imageType);
            response.addHeader("Content-Type", "image/" + imageType);
            outputStream.write(bytes);
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().println(JSONUtil.toJsonStr(ResponseEntity.fail("create image error", 500)));
            return;
        }
    }

    @GetMapping("/md5")
    public ResponseEntity<String> md5(@RequestParam String text) throws Exception {
        return ResponseEntity.success(SecureUtil.md5(text));
    }

    @GetMapping("/ipRegion")
    public ResponseEntity<String> ipRegion(@RequestParam String ip) throws Exception {
        return ResponseEntity.success(Ip2RegionUtil.ip2Region(ip));
    }

    @GetMapping("/genUuids")
    public ResponseEntity<List<String>> genUuid(@RequestParam int uuidCount) {
        List<String> uids = new ArrayList<>();
        for (int i = 0; i < uuidCount; i++) {
            uids.add(IdUtil.fastSimpleUUID());
        }
        return ResponseEntity.success(uids);
    }
}
