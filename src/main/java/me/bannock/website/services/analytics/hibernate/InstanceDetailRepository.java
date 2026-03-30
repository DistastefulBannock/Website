package me.bannock.website.services.analytics.hibernate;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InstanceDetailRepository extends JpaRepository<InstanceDetailEntity, Long> {

    Optional<InstanceDetailEntity> findInstanceDetailEntityByInstanceIdAndAndName(long instanceId, String name);

}
