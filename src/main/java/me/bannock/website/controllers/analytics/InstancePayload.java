package me.bannock.website.controllers.analytics;

import java.util.Map;

public class InstancePayload {

    private String instanceId;
    private Map<String, String> loggedData;

    public String getInstanceId() {
        return instanceId;
    }

    public Map<String, String> getLoggedData() {
        return loggedData;
    }

}
