package ru.tikonovns.capstone.spring.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.tikonovns.capstone.spring.dto.response.NotificationResponse;
import ru.tikonovns.capstone.spring.entity.Notification;

import java.util.List;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    @Mapping(target = "ticketId", source = "ticket.id")
    @Mapping(target = "ticketNumber", source = "ticket.ticketNumber")
    NotificationResponse toResponse(Notification notification);

    List<NotificationResponse> toResponseList(List<Notification> notifications);
}