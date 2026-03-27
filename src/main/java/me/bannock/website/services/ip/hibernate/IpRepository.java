package me.bannock.website.services.ip.hibernate;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IpRepository extends JpaRepository<IpEntity, Long> {

    Optional<IpEntity> getIpEntityByIpEqualsAndMillisExpiredGreaterThanEqual(String ip, long millisExpired);

}
