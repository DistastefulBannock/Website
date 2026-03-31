package me.bannock.website.services.analytics.hibernate;

import me.bannock.website.services.analytics.AnalyticsService;
import me.bannock.website.services.ip.IpThreatScoreService;
import me.bannock.website.services.webhooks.WebhookService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
public class HibernateAnalyticsServiceImpl implements AnalyticsService {

    @Autowired
    public HibernateAnalyticsServiceImpl(InstanceRepository instanceRepository,
                                         InstanceDetailRepository instanceDetailRepository,
                                         IpThreatScoreService ipThreatScoreService,
                                         WebhookService webhookService){
        this.instanceRepository = instanceRepository;
        this.instanceDetailRepository = instanceDetailRepository;
        this.ipThreatScoreService = ipThreatScoreService;
        this.webhookService = webhookService;
    }

    private final Logger logger = LogManager.getLogger();
    private final InstanceRepository instanceRepository;
    private final InstanceDetailRepository instanceDetailRepository;
    private final IpThreatScoreService ipThreatScoreService;
    private final WebhookService webhookService;

    @Value("${bannock.analytics.idHashSalt}")
    public String instanceIdHashSalt;

    @Override
    @Transactional
    public String createInstance(String ip, Map<String, String> details) {
        Objects.requireNonNull(ip);
        Objects.requireNonNull(details);
        InstanceEntity newInstance = new InstanceEntity(ip);
        newInstance = instanceRepository.saveAndFlush(newInstance);

        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] idHash = "%s%d".formatted(instanceIdHashSalt, newInstance.getInstanceId()).getBytes(StandardCharsets.UTF_8);
            idHash = digest.digest(idHash);
            String idHashString = new String(Hex.encode(idHash));
            newInstance.setInstanceIdHash(idHashString);
        } catch (NoSuchAlgorithmException e) {
            logger.error("Could not find hashing algorithm for id, rolling back, ip={}", ip);
            throw new RuntimeException(e);
        }

        List<InstanceDetailEntity> detailsEntities = newInstance.getDetails();
        for (String id : details.keySet()){
            InstanceDetailEntity newDetail = new InstanceDetailEntity(id, details.get(id));
            detailsEntities.add(newDetail);
        }

        newInstance = instanceRepository.save(newInstance);
        return newInstance.getInstanceIdHash();
    }

    @Override
    @Transactional
    public void scanIp(String instanceId) {
        Objects.requireNonNull(instanceId);
        Optional<InstanceEntity> instanceEntityOptional = instanceRepository
                .findInstanceEntityByInstanceIdHashEqualsAndMillisExpiredGreaterThanEqual(instanceId, System.currentTimeMillis());
        if (instanceEntityOptional.isEmpty()){
            logger.warn("Ip scan requested for unknown instance id hash, instanceId={}", instanceId);
            return;
        }

        InstanceEntity instanceEntity = instanceEntityOptional.get();
        int threatScore = ipThreatScoreService.getThreatScore(instanceEntity.getIp());
        Map<String, String> derivedDetails = new HashMap<>();
        derivedDetails.put("Proxy", "%b".formatted((threatScore & IpThreatScoreService.IS_PROXY) != 0));
        derivedDetails.put("Tor", "%b".formatted((threatScore & IpThreatScoreService.IS_TOR) != 0));
        derivedDetails.put("Sus ip", "%b".formatted((threatScore & IpThreatScoreService.IS_MALICIOUS) != 0));
        derivedDetails.put("Blacklisted ip", "%b".formatted((threatScore & IpThreatScoreService.IS_BLACKLISTED) != 0));
        for (String id : derivedDetails.keySet()){
            instanceEntity.getDetails().add(new InstanceDetailEntity(id, derivedDetails.get(id)));
        }
        instanceEntity = instanceRepository.saveAndFlush(instanceEntity);

        Optional<InstanceDetailEntity> pathDetail = instanceDetailRepository.findInstanceDetailEntityByInstanceIdAndAndName(
                instanceEntity.getInstanceId(), "Path");
        if (pathDetail.isPresent() && pathDetail.get().getValue().equals("/about/")){
            sendTelemetryNotification(instanceEntity, derivedDetails);
        }
    }

    private void sendTelemetryNotification(InstanceEntity instanceEntity, Map<String, String> derivedDetails) {
        Map<String, String> loggedDetails = new LinkedHashMap<>();
        loggedDetails.put("IP", "```%s```".formatted(instanceEntity.getIp()));
        for (String key : derivedDetails.keySet()){
            loggedDetails.put(key, "```%s```".formatted(derivedDetails.get(key)));
        }

        HashSet<String> detailsToInclude = new HashSet<>();
        detailsToInclude.addAll(Arrays.asList("Path", "Referer", "Display width", "Display height", "User agent",
                "Canvas hash", "Language", "Timezone", "Platform", "Do not track header"));
        for (InstanceDetailEntity details : instanceEntity.getDetails()){
            if (!detailsToInclude.contains(details.getName()) || details.getValue() == null || details.getValue().isEmpty())
                continue;
            boolean useJSNamePrefix = !details.getName().equals("Path") && !details.getName().equals("Referer");
            loggedDetails.put((useJSNamePrefix ? "JS_" : "") + details.getName(), "```%s```".formatted(details.getValue()));
            detailsToInclude.remove(details.getName());
            if (detailsToInclude.isEmpty())
                break;
        }

        Map<String, String> ipAttributes = ipThreatScoreService.getIpAttributes(instanceEntity.getIp());
        for (String key : new String[]{"region", "city", "timezone", "latitude", "longitude", "fraud_score",
                "zip_code", "ISP", "organization"}){
            if (!ipAttributes.containsKey(key) || ipAttributes.get(key).isEmpty())
                continue;
            loggedDetails.put("IP_" + key, "```%s```".formatted(ipAttributes.get(key)));
        }

        webhookService.sendNotification("About page request", "", loggedDetails);
    }

    @Override
    @Transactional
    public void addDetails(String instanceId, Map<String, String> details) {
        Objects.requireNonNull(instanceId);
        Objects.requireNonNull(details);
        Optional<InstanceEntity> instanceEntity = instanceRepository
                .findInstanceEntityByInstanceIdHashEqualsAndMillisExpiredGreaterThanEqual(instanceId, System.currentTimeMillis());
        if (instanceEntity.isEmpty()){
            logger.warn("Analytics data being added to invalid instance id hash, instanceId={}", instanceId);
            return;
        }

        for (String id : details.keySet()){
            instanceEntity.get().getDetails().add(new InstanceDetailEntity(id, details.get(id)));
        }
        instanceRepository.saveAndFlush(instanceEntity.get());
    }

}
