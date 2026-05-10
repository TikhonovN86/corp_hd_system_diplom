package ru.tikonovns.capstone.spring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.tikonovns.capstone.spring.entity.UserWorkGroup;
import ru.tikonovns.capstone.spring.entity.UserWorkGroupId;

import java.util.List;
import java.util.Optional;

public interface UserWorkGroupRepository extends JpaRepository<UserWorkGroup, UserWorkGroupId> {

    List<UserWorkGroup> findAllByUserId(Long userId);

    List<UserWorkGroup> findAllByWorkGroupId(Long workGroupId);

    boolean existsByUserIdAndWorkGroupId(Long userId, Long workGroupId);

    Optional<UserWorkGroup> findByUserIdAndWorkGroupId(Long userId, Long workGroupId);

    @Query("""
        select uwg
        from UserWorkGroup uwg
        join fetch uwg.user
        join fetch uwg.workGroup
        where uwg.user.id = :userId
    """)
    List<UserWorkGroup> findAllByUserIdWithWorkGroup(@Param("userId") Long userId);
}