package io.github.springstudent.util;

import org.lionsoul.ip2region.DbConfig;
import org.lionsoul.ip2region.DbSearcher;
import org.lionsoul.ip2region.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * @author zhouning
 * @date 2023/08/16 10:35
 */
public class Ip2RegionUtil {
    private static final Logger logger = LoggerFactory.getLogger(Ip2RegionUtil.class);

    private static DbSearcher searcher;

    private static final String UNKNOWN = "未知";

    static {
        try {
            String dbPath = Ip2RegionUtil.class.getResource("/ip2region.db").getPath();
            File file = new File(dbPath);
            if (!file.exists()) {
                logger.error("Error: Invalid ip2region.db file");
            }
            searcher = new DbSearcher(new DbConfig(), dbPath);
        } catch (Exception e) {
            logger.error("初始化searcher失败", e);
        }
    }

    /**
     * ip地址归属地
     *
     * @return String ip归属地
     */
    public static String ip2Region(String ip) {
        //中国|0|甘肃省|平凉市|电信
        //0|0|0|内网IP|内网IP
        String result = UNKNOWN;
        try {
            if (Util.isIpAddress(ip)) {
                String region = searcher.btreeSearch(ip).getRegion();
                if (region.indexOf("|") != -1) {
                    String[] regionArr = region.split("\\|");
                    if (regionArr.length == 5) {
                        if (regionArr[3].indexOf("内网") != -1 || regionArr[4].indexOf("内网") != -1) {
                            result = "局域网";
                        } else {
                            result = regionArr[0] + regionArr[2] + regionArr[3];
                            if (!regionArr[4].equals("0")) {
                                result += regionArr[4];
                            }
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) {
            logger.error("文件【ip2region.db】不存在，解析失败");
        } catch (Exception e) {
            logger.error("ip地址无法解析,ip={}", ip, e);
        }
        return result;
    }

}
