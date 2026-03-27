package me.bannock.website.services.ip.hibernate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "core_ip_threat_score_attributes", indexes = {
        @Index(columnList = "attribute_id", unique = true),
        @Index(columnList = "score_id")
})
public class IpAttributesEntity {

    public IpAttributesEntity(){}

    public IpAttributesEntity(String name, String value){
        this.name = name;
        this.value = value;
    }

    @Id
    @GeneratedValue(generator = "core_ip_threat_score_attributes_seq")
    @Column(name = "attribute_id", nullable = false)
    @SequenceGenerator(name = "core_ip_threat_score_attributes_seq",
            sequenceName = "core_ip_threat_score_attributes_seq", initialValue = 0, allocationSize = 1)
    private long attributeId;

    @Column(name = "name")
    private String name;

    @Column(name = "value")
    private String value;

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

}
