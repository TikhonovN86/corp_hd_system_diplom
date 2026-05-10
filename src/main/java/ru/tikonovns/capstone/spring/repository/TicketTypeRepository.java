package ru.tikonovns.capstone.spring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.tikonovns.capstone.spring.entity.TicketType;

import java.util.Optional;

public interface TicketTypeRepository extends JpaRepository<TicketType, Long> {

    Optional<TicketType> findByCode(String code);

    Optional<TicketType> findByName(String name);

    boolean existsByCode(String code);
}