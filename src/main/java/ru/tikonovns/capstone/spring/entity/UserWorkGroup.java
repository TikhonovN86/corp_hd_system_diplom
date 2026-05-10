package ru.tikonovns.capstone.spring.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_work_groups")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserWorkGroup {

    @EmbeddedId
    private UserWorkGroupId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)

    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)

    @MapsId("workGroupId")
    @JoinColumn(name = "work_group_id", nullable = false)
    private WorkGroup workGroup;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}