package ru.tikonovns.capstone.spring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.tikonovns.capstone.spring.entity.WorkGroup;

import java.util.Optional;

public interface WorkGroupRepository extends JpaRepository<WorkGroup, Long> {

    Optional<WorkGroup> findByCode(String code);

    Optional<WorkGroup> findByName(String name);

    boolean existsByCode(String code);
}