package ru.tikonovns.capstone.spring.mapper;

import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.tikonovns.capstone.spring.dto.response.TicketResponse;
import ru.tikonovns.capstone.spring.entity.Ticket;
import ru.tikonovns.capstone.spring.entity.User;
import ru.tikonovns.capstone.spring.service.SlaService;

import java.util.List;

@Mapper(componentModel = "spring", uses = SlaService.class)
public interface TicketMapper {

    @Named("fullTicketResponse")
    @Mapping(target = "initiatorId", source = "initiator.id")
    @Mapping(target = "initiatorUsername", source = "initiator.username")
    @Mapping(target = "initiatorFullName", source = "initiator", qualifiedByName = "userToFullName")

    @Mapping(target = "sectionId", source = "section.id")
    @Mapping(target = "sectionCode", source = "section.code")
    @Mapping(target = "sectionName", source = "section.name")

    @Mapping(target = "serviceDirectionId", source = "serviceDirection.id")
    @Mapping(target = "serviceDirectionCode", source = "serviceDirection.code")
    @Mapping(target = "serviceDirectionName", source = "serviceDirection.name")

    @Mapping(target = "ticketTypeId", source = "ticketType.id")
    @Mapping(target = "ticketTypeCode", source = "ticketType.code")
    @Mapping(target = "ticketTypeName", source = "ticketType.name")

    @Mapping(target = "workGroupId", source = "workGroup.id")
    @Mapping(target = "workGroupCode", source = "workGroup.code")
    @Mapping(target = "workGroupName", source = "workGroup.name")

    @Mapping(target = "assigneeId", source = "assignee.id")
    @Mapping(target = "assigneeUsername", source = "assignee.username")
    @Mapping(target = "assigneeFullName", source = "assignee", qualifiedByName = "userToFullName")

    @Mapping(target = "status", source = "status")

    @Mapping(target = "slaOverdue", ignore = true)
    TicketResponse toResponse(Ticket ticket);

    @Mapping(target = "initiatorId", source = "initiator.id")
    @Mapping(target = "initiatorUsername", source = "initiator.username")
    @Mapping(target = "initiatorFullName", source = "initiator", qualifiedByName = "userToFullName")

    @Mapping(target = "sectionId", source = "section.id")
    @Mapping(target = "sectionCode", source = "section.code")
    @Mapping(target = "sectionName", source = "section.name")

    @Mapping(target = "serviceDirectionId", source = "serviceDirection.id")
    @Mapping(target = "serviceDirectionCode", source = "serviceDirection.code")
    @Mapping(target = "serviceDirectionName", source = "serviceDirection.name")

    @Mapping(target = "ticketTypeId", source = "ticketType.id")
    @Mapping(target = "ticketTypeCode", source = "ticketType.code")
    @Mapping(target = "ticketTypeName", source = "ticketType.name")

    @Named("initiatorTicketResponse")
    @Mapping(target = "workGroupId", ignore = true)
    @Mapping(target = "workGroupCode", ignore = true)
    @Mapping(target = "workGroupName", ignore = true)

    @Mapping(target = "assigneeId", ignore = true)
    @Mapping(target = "assigneeUsername", ignore = true)
    @Mapping(target = "assigneeFullName", ignore = true)

    @Mapping(target = "status", source = "status")

    @Mapping(target = "slaOverdue", ignore = true)
    TicketResponse toInitiatorResponse(Ticket ticket);

    @IterableMapping(qualifiedByName = "fullTicketResponse")
    List<TicketResponse> toResponseList(List<Ticket> tickets);

    @IterableMapping(qualifiedByName = "initiatorTicketResponse")
    List<TicketResponse> toInitiatorResponseList(List<Ticket> tickets);

    @Named("userToFullName")
    default String userToFullName(User user) {
        if (user == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();

        if (user.getLastName() != null && !user.getLastName().isBlank()) {
            sb.append(user.getLastName());
        }
        if (user.getFirstName() != null && !user.getFirstName().isBlank()) {
            if (!sb.isEmpty()) {
                sb.append(" ");
            }
            sb.append(user.getFirstName());
        }
        if (user.getMiddleName() != null && !user.getMiddleName().isBlank()) {
            if (!sb.isEmpty()) {
                sb.append(" ");
            }
            sb.append(user.getMiddleName());
        }

        return sb.toString();
    }
}