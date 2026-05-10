package ru.tikonovns.capstone.spring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.tikonovns.capstone.spring.entity.TicketEvent;

import java.util.List;

public interface TicketEventRepository extends JpaRepository<TicketEvent, Long> {

    @Query("""
        select te
        from TicketEvent te
        join fetch te.author
        where te.ticket.id = :ticketId
        order by te.createdAt asc
    """)
    List<TicketEvent> findAllByTicketIdOrderByCreatedAtAsc(@Param("ticketId") Long ticketId);
}