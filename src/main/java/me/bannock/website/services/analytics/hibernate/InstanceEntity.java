package me.bannock.website.services.analytics.hibernate;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import me.bannock.website.services.ip.hibernate.IpAttributesEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "analytics_instances", indexes = {
        @Index(columnList = "instance_id", unique = true),
        @Index(columnList = "instance_id_hash, millis_expired"),
        @Index(columnList = "millis_captured")
})
public class InstanceEntity {

    /**
     * How long instances exist before their details should no longer be modified
     */
    private static final long INSTANCE_EXPIRATION_MILLIS = 30000;

    public InstanceEntity(){}

    public InstanceEntity(String ip){
        Objects.requireNonNull(ip);
        this.ip = ip;
        this.millisExpired = System.currentTimeMillis() + INSTANCE_EXPIRATION_MILLIS;
        this.millisCaptured = System.currentTimeMillis();
    }

    @Id
    @GeneratedValue(generator = "analytics_instances_seq")
    @Column(name = "instance_id", nullable = false)
    @SequenceGenerator(name = "analytics_instances_seq", sequenceName = "analytics_instances_seq",
            initialValue = 0, allocationSize = 1)
    private Long instanceId;

    @Column(name = "instance_id_hash", length = 64)
    private String instanceIdHash;

    @Column(name = "ip", length = 45, nullable = false)
    private String ip;

    @Column(name = "millis_expired", nullable = false)
    private long millisExpired;

    @Column(name = "millis_captured", nullable = false)
    private long millisCaptured;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "instance_id")
    private List<InstanceDetailEntity> details;

    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceIdHash(String instanceIdHash) {
        this.instanceIdHash = instanceIdHash;
    }

    public String getInstanceIdHash() {
        return instanceIdHash;
    }

    public String getIp() {
        return ip;
    }

    public long getMillisExpired() {
        return millisExpired;
    }

    public long getMillisCaptured() {
        return millisCaptured;
    }

    public List<InstanceDetailEntity> getDetails() {
        if (details == null)
            this.details = new ArrayList<>();
        return details;
    }

}
