package ru.tikonovns.capstone.spring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.tikonovns.capstone.spring.entity.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("""
        select c
        from Comment c
        join fetch c.author
        where c.ticket.id = :ticketId
        order by c.createdAt asc
    """)
    List<Comment> findAllByTicketIdOrderByCreatedAtAsc(@Param("ticketId") Long ticketId);

    @Query("""
        select c
        from Comment c
        join fetch c.author
        where c.ticket.id = :ticketId
          and c.isPrivate = false
        order by c.createdAt asc
    """)
    List<Comment> findPublicCommentsByTicketIdOrderByCreatedAtAsc(@Param("ticketId") Long ticketId);

    long countByTicketId(Long ticketId);
}