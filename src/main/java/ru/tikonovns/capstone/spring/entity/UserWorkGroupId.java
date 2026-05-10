package ru.tikonovns.capstone.spring.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor

public class UserWorkGroupId implements Serializable {

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "work_group_id")
    private Long workGroupId;
}
