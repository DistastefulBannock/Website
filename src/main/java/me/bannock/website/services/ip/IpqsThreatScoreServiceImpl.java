package me.bannock.website.services.ip;

import me.bannock.website.services.ip.hibernate.IpAttributesEntity;
import me.bannock.website.services.ip.hibernate.IpEntity;
import me.bannock.website.services.ip.hibernate.IpRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
public class IpqsThreatScoreServiceImpl implements IpThreatScoreService {

    @Autowired
    public IpqsThreatScoreServiceImpl(IpRepository ipRepository){
        this.ipRepository = ipRepository;
        restClient = RestClient.builder()
                .baseUrl("https://ipqualityscore.com/api/json/ip/")
                .defaultHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) " +
                        "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/135.0.0.0 Safari/537.36")
                .build();
    }

    private final Logger logger = LogManager.getLogger();
    private final IpRepository ipRepository;
    private final RestClient restClient;
    @Value("${bannock.ipqs.apikey}")
    public String apiKey;
    @Value("${bannock.ipqs.maxThreatScore}")
    public int maxThreatScore;
    @Value("${bannock.ipqs.threatExpirationMillis}")
    public long threatExpirationMillis;

    private int calculateThreatScore(String ip){
        String ipDetails = restClient.get().uri("%s/%s".formatted(apiKey, ip)).retrieve().body(String.class);
        JSONObject ipDetailsJson = new JSONObject(ipDetails);
        int threatScore = 0;
        try{
            if (ipDetailsJson.getBoolean("proxy") || ipDetailsJson.getBoolean("vpn")
                    || ipDetailsJson.getBoolean("active_vpn"))
                threatScore |= IS_PROXY;
        }catch (JSONException e){
            logger.warn("Could not find proxy attributes in ipqs response", e);
            return -1;
        }
        try{
            if (ipDetailsJson.getBoolean("tor") || ipDetailsJson.getBoolean("active_tor"))
                threatScore |= IS_TOR;
        }catch (JSONException e){
            logger.warn("Could not find tor attributes in ipqs response", e);
            return -1;
        }
        try{
            if (ipDetailsJson.getInt("fraud_score") >= maxThreatScore)
                threatScore |= IS_MALICIOUS;
        }catch (JSONException e){
            logger.warn("Could not find fraud score attribute in ipqs response", e);
            return -1;
        }

        IpEntity newIpEntity = new IpEntity(ip, threatExpirationMillis, threatScore);
        for (String name : ipDetailsJson.keySet()){
            try{
                newIpEntity.getAttributes().add(new IpAttributesEntity(name, ipDetailsJson.get(name).toString()));
            }catch (JSONException e){
                logger.warn("Failed to log attribute, name={}", name, e);
            }
        }
        ipRepository.saveAndFlush(newIpEntity);

        return threatScore;
    }

    @Override
    public int getThreatScore(String ip) {
        Objects.requireNonNull(ip);

        Optional<IpEntity> cacheThreatScore = ipRepository
                .getIpEntityByIpEqualsAndMillisExpireGreaterThanEqual(ip, System.currentTimeMillis());
        int threatScore = -1;
        if (cacheThreatScore.isEmpty()){
            threatScore = calculateThreatScore(ip);
        }else{
            threatScore = cacheThreatScore.get().getThreatScore();
        }

        return threatScore;
    }

    @Override
    public Map<String, String> getIpAttributes() {
        return Map.of();
    }

}
