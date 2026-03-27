package me.bannock.website.services.analytics;

import org.springframework.scheduling.annotation.Async;

import java.util.Map;

public interface AnalyticsService {

    /**
     * @param ip The ip address to log
     * @param details Any occupying details with the request
     * @return The id of the analytics instance, or null if ir could not be created
     */
    String createInstance(String ip, Map<String, String> details);

    /**
     * Scans the ip address of an analytics instance for data
     * @param instanceId The instance to scan
     */
    @Async
    void scanIp(String instanceId);

    /**
     * Adds details to a request
     * @param instanceId The analytics instance id
     * @param details The details to add to this instance
     */
    @Async
    void addDetails(String instanceId, Map<String, String> details);



}
