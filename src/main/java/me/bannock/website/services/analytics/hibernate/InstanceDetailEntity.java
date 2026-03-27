package me.bannock.website.services.analytics.hibernate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "analytics_instances_details", indexes = {
        @Index(columnList = "detail_id")
})
public class InstanceDetailEntity {

    public InstanceDetailEntity(){}

    public InstanceDetailEntity(String name, String value){
        this.name = name;
        this.value = value;
    }

    @Id
    @GeneratedValue(generator = "analytics_instances_details_seq")
    @Column(name = "detail_id", nullable = false)
    @SequenceGenerator(name = "analytics_instances_details_seq",
            sequenceName = "analytics_instances_details_seq", initialValue = 0, allocationSize = 1)
    private long detailId;

    @Column(name = "name")
    private String name;

    @Column(name = "value")
    private String value;

    public long getDetailId() {
        return detailId;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

}
