package me.bannock.website.controllers.analytics;

import brave.Tracer;
import jakarta.servlet.http.HttpServletRequest;
import me.bannock.website.services.analytics.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

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
    public String getAnalyticsInstance(HttpServletRequest request){
        if (request.getRequestURI().equalsIgnoreCase("/analytics/callback")){
            return null;
        }
        String remoteIp = request.getRemoteAddr();
        if (useCloudflareIpHeaderWhenAvailable && request.getHeader("CF-Connecting-IP") != null){
            remoteIp = request.getHeader("CF-Connecting-IP");
        }

        Map<String, String> requestData = new HashMap<>();
        requestData.put("tId", tracer.currentSpan().context().traceIdString());
        requestData.put("Path", request.getRequestURI());
        requestData.put("Method", request.getMethod());
        requestData.put("Initial request timestamp", new Date(System.currentTimeMillis()).toString());

        return analyticsService.createInstance(remoteIp, requestData);
    }

}
