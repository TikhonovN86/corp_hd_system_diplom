package ru.tikonovns.capstone.spring.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
        name = "service_directions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_service_direction_name_per_section",
                        columnNames = {"section_id", "name"}
                )
        }
)
public class ServiceDirection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "section_id")
    private Section section;

    @Column(name = "code")
    private String code;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "default_ticket_type_id")
    private TicketType defaultTicketType;
}