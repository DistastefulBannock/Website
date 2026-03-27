package me.bannock.website.services.analytics.hibernate;

import me.bannock.website.services.analytics.AnalyticsService;
import me.bannock.website.services.ip.IpThreatScoreService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
public class HibernateAnalyticsServiceImpl implements AnalyticsService {

    @Autowired
    public HibernateAnalyticsServiceImpl(InstanceRepository instanceRepository, IpThreatScoreService ipThreatScoreService){
        this.instanceRepository = instanceRepository;
        this.ipThreatScoreService = ipThreatScoreService;
    }

    private final Logger logger = LogManager.getLogger();
    private final InstanceRepository instanceRepository;
    private final IpThreatScoreService ipThreatScoreService;

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
