package io.github.springstudent.bean;
import cn.hutool.json.JSONUtil;
import io.github.springstudent.util.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
/**
 * @author zhouning
 * @date 2023/08/21 10:31
 */
@Aspect
@Component
@Slf4j
public class InterfaceLimitAspect {
    private static ConcurrentHashMap<String, ExpiringMap<String, Integer>> book = new ConcurrentHashMap<>();

    @Resource
    private HttpServletResponse response;
    /**
     * 层切点
     */
    @Pointcut("@annotation(interfaceLimit)")
    public void controllerAspect(InterfaceLimit interfaceLimit) {
    }

    @Around("controllerAspect(interfaceLimit)")
    public Object doAround(ProceedingJoinPoint pjp, InterfaceLimit interfaceLimit) throws Throwable {
        // 获得request对象
        RequestAttributes ra = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes sra = (ServletRequestAttributes) ra;
        HttpServletRequest request = sra.getRequest();
        String ip = HttpUtil.getIpAddr(request);
        ExpiringMap<String, Integer> uc = book.getOrDefault(request.getRequestURI(), ExpiringMap.builder().variableExpiration().build());
        Integer uCount = uc.getOrDefault(ip, 0);
        if (uCount >= interfaceLimit.value()) {
            log.error("接口拦截：{} 请求超过限制频率【{}次/{}ms】,IP为{}", request.getRequestURI(), interfaceLimit.value(), interfaceLimit.time(),ip);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(JSONUtil.toJsonStr(ResponseEntity.fail("请求频次超过限制", 500)));
            return null;
        } else if (uCount == 0) {
            uc.put(ip, uCount + 1, ExpirationPolicy.CREATED, interfaceLimit.time(), TimeUnit.MILLISECONDS);
        } else {
            uc.put(ip, uCount + 1);
        }
        book.put(request.getRequestURI(), uc);
        return pjp.proceed();
    }
}
