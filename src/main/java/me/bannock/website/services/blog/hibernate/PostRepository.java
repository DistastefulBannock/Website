package me.bannock.website.services.blog.hibernate;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<PostEntity, Long> {

    /**
     * @param pageable The page
     * @return a given amount of posts ordered the date they were posted desc
     */
    List<PostEntity> findByOrderByMillisPostedDesc(Pageable pageable);

}
