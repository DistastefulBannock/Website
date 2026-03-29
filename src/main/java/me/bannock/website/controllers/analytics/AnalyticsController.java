package me.bannock.website.controllers.analytics;

import me.bannock.website.services.analytics.AnalyticsService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/analytics")
public class AnalyticsController {

    @Autowired
    public AnalyticsController(AnalyticsService analyticsService){
        this.analyticsService = analyticsService;
    }

    private final Logger logger = LogManager.getLogger();
    private final AnalyticsService analyticsService;

    @PatchMapping("/callback")
    public ResponseEntity<Void> handleCallbackEndpoint(@RequestBody InstancePayload loggedData){
        String instanceId = loggedData.getInstanceId();
        if (instanceId == null) {
            logger.warn("Received analytic callback request without instance id");
            return ResponseEntity.badRequest().build();
        }
        analyticsService.scanIp(instanceId);
        analyticsService.addDetails(instanceId, loggedData.getLoggedData());

        return ResponseEntity.ok().build();
    }

}
