package ru.tikonovns.capstone.spring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.tikonovns.capstone.spring.entity.Notification;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("""
        select n
        from Notification n
        join fetch n.ticket
        where n.user.id = :userId
        order by n.createdAt desc
    """)
    List<Notification> findAllByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    @Query("""
        select n
        from Notification n
        join fetch n.ticket
        where n.user.id = :userId
          and n.isRead = false
        order by n.createdAt desc
    """)
    List<Notification> findUnreadByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    long countByUserIdAndIsReadFalse(Long userId);
}