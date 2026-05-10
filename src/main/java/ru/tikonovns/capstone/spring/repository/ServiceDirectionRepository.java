package ru.tikonovns.capstone.spring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.tikonovns.capstone.spring.entity.ServiceDirection;

import java.util.List;
import java.util.Optional;

public interface ServiceDirectionRepository extends JpaRepository<ServiceDirection, Long> {

    Optional<ServiceDirection> findByCode(String code);

    @Query("""
        select sd
        from ServiceDirection sd
        join fetch sd.section
        join fetch sd.defaultTicketType
        where sd.id = :id
    """)
    Optional<ServiceDirection> findByIdWithReferences(@Param("id") Long id);

    @Query("""
        select sd
        from ServiceDirection sd
        where sd.section.id = :sectionId
        order by sd.name asc
    """)
    List<ServiceDirection> findAllBySectionId(@Param("sectionId") Long sectionId);

    @Query("""
        select sd
        from ServiceDirection sd
        where sd.section.code = :sectionCode
        order by sd.name asc
    """)
    List<ServiceDirection> findAllBySectionCode(@Param("sectionCode") String sectionCode);
}