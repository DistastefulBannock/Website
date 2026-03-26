package me.bannock.website.services.ip;

import java.util.Map;

public interface IpThreatScoreService {

    static final int IS_PROXY = 1;
    static final int IS_TOR = 1 << 1;
    static final int IS_MALICIOUS = 1 << 2;
    static final int IS_BLACKLISTED = 1 << 3;

    /**
     * @param ip The ip to get the threat score for
     * @return -1 if invalid, otherwise bitmask of fields in this class
     */
    int getThreatScore(String ip);

    /**
     * @return Whatever information the ip rating service provides
     */
    Map<String, String> getIpAttributes();

}
