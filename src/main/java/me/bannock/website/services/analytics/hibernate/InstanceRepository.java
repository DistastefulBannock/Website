package me.bannock.website.services.analytics.hibernate;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InstanceRepository extends JpaRepository<InstanceEntity, Long> {

    Optional<InstanceEntity> findInstanceEntityByInstanceIdHashEqualsAndMillisExpiredGreaterThanEqual(String instanceHash, long currentMillis);

}
