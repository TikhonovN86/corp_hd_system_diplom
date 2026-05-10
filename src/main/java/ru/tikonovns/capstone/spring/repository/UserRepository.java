package ru.tikonovns.capstone.spring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.tikonovns.capstone.spring.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    @Query("""
                select distinct u
                from User u
                left join fetch u.role
                left join fetch u.userWorkGroups uwg
                left join fetch uwg.workGroup
                where u.username = :username
            """)
    Optional<User> findByUsernameWithRoleAndGroups(String username);

    @Query("""
                select distinct u
                from User u
                left join fetch u.role
                left join fetch u.userWorkGroups uwg
                left join fetch uwg.workGroup
                where u.id = :id
            """)
    Optional<User> findByIdWithRoleAndGroups(@Param("id") Long id);

    @Query("""
                select distinct u
                from User u
                join u.userWorkGroups uwg
                where uwg.workGroup.id = :workGroupId
                order by u.lastName asc, u.firstName asc, u.middleName asc
            """)
    List<User> findAllByWorkGroupId(@Param("workGroupId") Long workGroupId);

    @Query("""
                select distinct u
                from User u
                join u.userWorkGroups uwg
                join uwg.workGroup wg
                where wg.code = :workGroupCode
                order by u.lastName asc, u.firstName asc, u.middleName asc
            """)
    List<User> findAllByWorkGroupCode(@Param("workGroupCode") String workGroupCode);
}
