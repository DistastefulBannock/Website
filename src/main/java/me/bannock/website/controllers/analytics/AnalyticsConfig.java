package me.bannock.website.controllers.analytics;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AnalyticsConfig implements WebMvcConfigurer {

    public AnalyticsConfig(AnalyticsInterceptor analyticsInterceptor) {
        this.analyticsInterceptor = analyticsInterceptor;
    }

    private final AnalyticsInterceptor analyticsInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(analyticsInterceptor).addPathPatterns("/**").excludePathPatterns("/analytics/callback");
    }
}
