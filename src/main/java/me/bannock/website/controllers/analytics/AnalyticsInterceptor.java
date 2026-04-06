package me.bannock.website.controllers.analytics;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import me.bannock.website.services.analytics.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.HashMap;

@Component
public class AnalyticsInterceptor implements HandlerInterceptor {

    @Autowired
    public AnalyticsInterceptor(AnalyticsService analyticsService){
        this.analyticsService = analyticsService;
    }

    private final AnalyticsService analyticsService;

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        Object trackingToken = request.getAttribute("mossadTrackingToken");
        if (trackingToken == null)
            return;

        HashMap<String, String> newDetails = new HashMap<>();
        newDetails.put("Response status code", "%d".formatted(response.getStatus()));
        String location = response.getHeader("Location");
        if (location != null){
            newDetails.put("Redirected to", location);
        }
        analyticsService.addDetails(trackingToken.toString(), newDetails);
    }

}
