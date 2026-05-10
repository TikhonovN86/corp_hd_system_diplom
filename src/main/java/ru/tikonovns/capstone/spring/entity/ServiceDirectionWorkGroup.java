package ru.tikonovns.capstone.spring.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "service_direction_work_groups")
public class ServiceDirectionWorkGroup {

    @EmbeddedId
    private ServiceDirectionWorkGroupId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("serviceDirectionId")
    @JoinColumn(name = "service_direction_id")
    private ServiceDirection serviceDirection;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("workGroupId")
    @JoinColumn(name = "work_group_id")
    private WorkGroup workGroup;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}