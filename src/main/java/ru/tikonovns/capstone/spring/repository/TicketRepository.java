package ru.tikonovns.capstone.spring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.tikonovns.capstone.spring.entity.Ticket;

import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findByTicketNumber(String ticketNumber);

    @Query("""
        select t
        from Ticket t
        join fetch t.initiator
        join fetch t.section
        join fetch t.serviceDirection
        join fetch t.ticketType
        join fetch t.workGroup
        left join fetch t.assignee
        where t.id = :id
    """)
    Optional<Ticket> findByIdWithAllReferences(@Param("id") Long id);

    @Query("""
        select t
        from Ticket t
        join fetch t.initiator
        join fetch t.workGroup
        left join fetch t.assignee
        where t.initiator.id = :initiatorId
        order by t.createdAt desc
    """)
    List<Ticket> findAllByInitiatorIdOrderByCreatedAtDesc(@Param("initiatorId") Long initiatorId);

    @Query("""
        select t
        from Ticket t
        join fetch t.initiator
        join fetch t.workGroup
        left join fetch t.assignee
        where t.workGroup.id = :workGroupId
        order by t.createdAt desc
    """)
    List<Ticket> findAllByWorkGroupIdOrderByCreatedAtDesc(@Param("workGroupId") Long workGroupId);

    @Query("""
        select t
        from Ticket t
        join fetch t.initiator
        join fetch t.workGroup
        left join fetch t.assignee
        where t.workGroup.code = :workGroupCode
        order by t.createdAt desc
    """)
    List<Ticket> findAllByWorkGroupCodeOrderByCreatedAtDesc(@Param("workGroupCode") String workGroupCode);

    @Query("""
        select t
        from Ticket t
        join fetch t.initiator
        join fetch t.workGroup
        left join fetch t.assignee
        where t.workGroup.id = :workGroupId
          and t.status = :status
        order by t.createdAt desc
    """)
    List<Ticket> findAllByWorkGroupIdAndStatusOrderByCreatedAtDesc(
            @Param("workGroupId") Long workGroupId,
            @Param("status") String status
    );

    @Query("""
        select t
        from Ticket t
        join fetch t.initiator
        join fetch t.workGroup
        left join fetch t.assignee
        where t.assignee.id = :assigneeId
        order by t.createdAt desc
    """)
    List<Ticket> findAllByAssigneeIdOrderByCreatedAtDesc(@Param("assigneeId") Long assigneeId);

    @Query("""
        select t
        from Ticket t
        join fetch t.initiator
        join fetch t.workGroup
        left join fetch t.assignee
        where t.assignee.id = :assigneeId
          and t.status in :statuses
        order by t.createdAt desc
    """)
    List<Ticket> findAllByAssigneeIdAndStatusesOrderByCreatedAtDesc(
            @Param("assigneeId") Long assigneeId,
            @Param("statuses") List<String> statuses
    );

    @Query("""
        select t
        from Ticket t
        where t.workGroup.id = :workGroupId
          and t.status = 'NEW'
          and t.assignee is null
        order by t.createdAt desc
    """)
    List<Ticket> findNewUnassignedByWorkGroupId(@Param("workGroupId") Long workGroupId);
}