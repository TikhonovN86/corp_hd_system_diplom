package ru.tikonovns.capstone.spring.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.tikonovns.capstone.spring.dto.response.dictionary.DictionaryItemResponse;
import ru.tikonovns.capstone.spring.dto.response.dictionary.UserDictionaryResponse;
import ru.tikonovns.capstone.spring.entity.*;

@Mapper(componentModel = "spring")
public interface DictionaryMapper {

    DictionaryItemResponse sectionToResponse(Section section);

    @Mapping(target = "description", source = "description")
    DictionaryItemResponse serviceDirectionToResponse(ServiceDirection serviceDirection);

    DictionaryItemResponse ticketTypeToResponse(TicketType ticketType);

    DictionaryItemResponse workGroupToResponse(WorkGroup workGroup);

    @Mapping(target = "fullName", expression = "java(buildFullName(user))")
    UserDictionaryResponse userToDictionaryResponse(User user);

    default String buildFullName(User user) {
        StringBuilder sb = new StringBuilder();

        if (user.getLastName() != null && !user.getLastName().isBlank()) {
            sb.append(user.getLastName());
        }
        if (user.getFirstName() != null && !user.getFirstName().isBlank()) {
            if (!sb.isEmpty()) sb.append(" ");
            sb.append(user.getFirstName());
        }
        if (user.getMiddleName() != null && !user.getMiddleName().isBlank()) {
            if (!sb.isEmpty()) sb.append(" ");
            sb.append(user.getMiddleName());
        }

        return sb.toString();
    }
}
