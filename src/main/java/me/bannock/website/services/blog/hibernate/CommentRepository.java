package me.bannock.website.services.blog.hibernate;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<CommentEntity, Long> {

    /**
     * Finds a given amount of comments using the comments' post id
     * @param postId The id of the post that the comments are under
     * @param pageable The page configuration for pagination
     * @return A list containing all the comments under the given post
     */
    List<CommentEntity> findByPostId(long postId, Pageable pageable);

}
