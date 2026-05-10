package ru.tikonovns.capstone.spring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.tikonovns.capstone.spring.entity.ServiceDirectionWorkGroup;
import ru.tikonovns.capstone.spring.entity.ServiceDirectionWorkGroupId;
import ru.tikonovns.capstone.spring.entity.WorkGroup;

import java.util.List;

public interface ServiceDirectionWorkGroupRepository extends JpaRepository<ServiceDirectionWorkGroup, ServiceDirectionWorkGroupId> {

    boolean existsByServiceDirectionIdAndWorkGroupId(Long serviceDirectionId, Long workGroupId);

    List<ServiceDirectionWorkGroup> findAllByServiceDirectionId(Long serviceDirectionId);

    List<ServiceDirectionWorkGroup> findAllByWorkGroupId(Long workGroupId);

    @Query("""
        select sdwg.workGroup
        from ServiceDirectionWorkGroup sdwg
        where sdwg.serviceDirection.id = :serviceDirectionId
        order by sdwg.workGroup.name asc
    """)
    List<WorkGroup> findWorkGroupsByServiceDirectionId(@Param("serviceDirectionId") Long serviceDirectionId);

    @Query("""
        select sdwg.workGroup
        from ServiceDirectionWorkGroup sdwg
        where sdwg.serviceDirection.code = :serviceDirectionCode
        order by sdwg.workGroup.name asc
    """)
    List<WorkGroup> findWorkGroupsByServiceDirectionCode(@Param("serviceDirectionCode") String serviceDirectionCode);
}