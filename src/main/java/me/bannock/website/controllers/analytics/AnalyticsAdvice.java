package me.bannock.website.controllers.analytics;

import brave.Tracer;
import jakarta.servlet.http.HttpServletRequest;
import me.bannock.website.security.UnauthorizedHandler;
import me.bannock.website.services.analytics.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class AnalyticsAdvice {

    @Autowired
    public AnalyticsAdvice(AnalyticsService analyticsService){
        this.analyticsService = analyticsService;
    }

    private final AnalyticsService analyticsService;

    @Autowired
    private Tracer tracer;

    @Value("${bannock.useCloudflareIpHeaderWhenAvailable}")
    private boolean useCloudflareIpHeaderWhenAvailable;

    @ModelAttribute("mossadTrackingToken")
    public String getAnalyticsInstance(HttpServletRequest request,
                                       @RequestHeader(value = "User-Agent", required = false) String userAgent,
                                       @RequestHeader(value = "Referer", required = false) String referer,
                                       @RequestHeader(value = "CF-Connecting-IP", required = false) String cloudflareIp,
                                       @RequestHeader(value = "Cf-Ray", required = false) String cloudflareRayId){
        if (request.getRequestURI().equalsIgnoreCase("/analytics/callback")){
            return null;
        }

        Map<String, String> requestData = new HashMap<>();
        requestData.put("tId", tracer.currentSpan().context().traceIdString());

        String queryParams = request.getQueryString();
        String path = request.getRequestURI();
        Object pathBeforeErrorRedirect = request.getAttribute(UnauthorizedHandler.ORIGINAL_PATH_ATTRIBUTE);
        if (pathBeforeErrorRedirect != null){
            requestData.put("Path", "%s".formatted(pathBeforeErrorRedirect));
            requestData.put("PathAfterError", path);
        }else if (queryParams == null){
            requestData.put("Path", "%s".formatted(path));
        }else{
            requestData.put("Path", "%s?%s".formatted(path, queryParams));
        }

        requestData.put("Method", request.getMethod());
        requestData.put("Initial request timestamp", new Date(System.currentTimeMillis()).toString());

        if (userAgent != null)
            requestData.put("User agent", userAgent);

        requestData.put("Referer", referer == null ? "null/direct request" : referer);
        if (useCloudflareIpHeaderWhenAvailable && cloudflareRayId != null){
            requestData.put("Ray id", cloudflareRayId);
        }

        String remoteIp = request.getRemoteAddr();
        if (useCloudflareIpHeaderWhenAvailable && cloudflareIp != null){
            remoteIp = cloudflareIp;
        }
        String mossadTrackingToken = analyticsService.createInstance(remoteIp, requestData);

        // We need this so the interceptor always has this to log
        request.setAttribute("mossadTrackingToken", mossadTrackingToken);
        return mossadTrackingToken;
    }

}
