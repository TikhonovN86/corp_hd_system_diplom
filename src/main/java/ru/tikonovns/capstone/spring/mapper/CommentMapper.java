package ru.tikonovns.capstone.spring.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.tikonovns.capstone.spring.dto.response.CommentResponse;
import ru.tikonovns.capstone.spring.entity.Comment;
import ru.tikonovns.capstone.spring.entity.User;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(target = "ticketId", source = "ticket.id")
    @Mapping(target = "authorId", source = "author.id")
    @Mapping(target = "authorUsername", source = "author.username")
    @Mapping(target = "authorFullName", source = "author", qualifiedByName = "userToFullName")
    CommentResponse toResponse(Comment comment);

    List<CommentResponse> toResponseList(List<Comment> comments);

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