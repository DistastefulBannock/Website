package me.bannock.website.services.ip.hibernate;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "core_ip_threat_scores", indexes = {
        @Index(columnList = "score_id", unique = true),
        @Index(columnList = "ip, millis_expire")
})
public class IpEntity {

    public IpEntity(){}

    public IpEntity(String ip, long threatExpirationMillis, int threatScore){
        Objects.requireNonNull(ip);
        this.ip = ip;
        this.millisExpire = System.currentTimeMillis() + threatExpirationMillis;
        this.millisScanned = System.currentTimeMillis();
        this.threatScore = threatScore;
    }

    @Id
    @GeneratedValue(generator = "core_ip_threat_scores_seq")
    @Column(name = "score_id", nullable = false)
    @SequenceGenerator(name = "core_ip_threat_scores_seq", sequenceName = "core_ip_threat_scores_seq",
            initialValue = 0, allocationSize = 1)
    private Long scoreId;

    @Column(name = "ip", length = 45, nullable = false)
    private String ip;

    @Column(name = "millis_expire", nullable = false)
    private long millisExpire;

    @Column(name = "millis_scanned", nullable = false)
    private long millisScanned;

    @Column(name = "score", nullable = false)
    private int threatScore;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "score_id")
    private List<IpAttributesEntity> attributes;

    public Long getScoreId() {
        return scoreId;
    }

    public String getIp() {
        return ip;
    }

    public long getMillisExpire() {
        return millisExpire;
    }

    public long getMillisScanned() {
        return millisScanned;
    }

    public int getThreatScore() {
        return threatScore;
    }

    public List<IpAttributesEntity> getAttributes() {
        if (attributes == null)
            attributes = new ArrayList<>();
        return attributes;
    }

}
