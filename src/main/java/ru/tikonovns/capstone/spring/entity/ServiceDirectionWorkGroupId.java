package ru.tikonovns.capstone.spring.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ServiceDirectionWorkGroupId implements Serializable {

    @Column(name = "service_direction_id")
    private Long serviceDirectionId;

    @Column(name = "work_group_id")
    private Long workGroupId;
}