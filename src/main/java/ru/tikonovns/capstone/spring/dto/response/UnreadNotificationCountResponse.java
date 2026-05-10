package ru.tikonovns.capstone.spring.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UnreadNotificationCountResponse {

    private long count;
}