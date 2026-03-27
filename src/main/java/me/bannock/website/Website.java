package me.bannock.website;

import me.bannock.website.services.analytics.AnalyticsService;
import me.bannock.website.services.ip.IpThreatScoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.HashMap;

@SpringBootApplication
public class Website {

    @Autowired
    public Website(AnalyticsService scoreService){
        scoreService.createInstance("1.1.1.1", new HashMap<>());
    }

    public static void main(String[] args) {
        SpringApplication.run(Website.class, args);
    }

}
