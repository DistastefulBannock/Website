package me.bannock.website.controllers.analytics;

import me.bannock.website.services.analytics.AnalyticsService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

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
    public ResponseEntity<Void> handleCallbackEndpoint(@RequestBody Map<String, Object> loggedData){
        String instanceId = loggedData.get("instanceId").toString();
        if (instanceId == null) {
            logger.warn("Received analytic callback request without instance id");
            return ResponseEntity.badRequest().build();
        }

        String loggedDataJson = loggedData.get("loggedData").toString();
        Map<String, Object> loggedDataMap = new JSONObject(loggedDataJson).toMap();
        Map<String, String> finalLoggedDataMap = new HashMap<>();
        for (String id : loggedDataMap.keySet()){
            finalLoggedDataMap.put(id, loggedDataMap.get(id).toString());
        }
        analyticsService.scanIp(instanceId);
        analyticsService.addDetails(instanceId, finalLoggedDataMap);

        return ResponseEntity.ok().build();
    }

}
