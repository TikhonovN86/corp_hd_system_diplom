package ru.tikonovns.capstone.spring.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.tikonovns.capstone.spring.dto.request.comment.AddPrivateCommentRequest;
import ru.tikonovns.capstone.spring.dto.request.comment.AddPublicCommentRequest;
import ru.tikonovns.capstone.spring.dto.request.ticket.*;
import ru.tikonovns.capstone.spring.dto.response.CommentResponse;
import ru.tikonovns.capstone.spring.dto.response.TicketActionsResponse;
import ru.tikonovns.capstone.spring.dto.response.TicketEventResponse;
import ru.tikonovns.capstone.spring.dto.response.TicketResponse;
import ru.tikonovns.capstone.spring.entity.Comment;
import ru.tikonovns.capstone.spring.entity.Ticket;
import ru.tikonovns.capstone.spring.mapper.CommentMapper;
import ru.tikonovns.capstone.spring.mapper.TicketEventMapper;
import ru.tikonovns.capstone.spring.mapper.TicketMapper;
import ru.tikonovns.capstone.spring.security.CurrentUserService;
import ru.tikonovns.capstone.spring.security.TicketAccessService;
import ru.tikonovns.capstone.spring.service.CommentService;
import ru.tikonovns.capstone.spring.service.SlaService;
import ru.tikonovns.capstone.spring.service.TicketEventService;
import ru.tikonovns.capstone.spring.service.TicketService;

import java.util.List;

import static ru.tikonovns.capstone.spring.utils.constants.UserRole.*;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;
    private final CommentService commentService;
    private final CurrentUserService currentUserService;
    private final TicketEventService ticketEventService;
    private final SlaService slaService;
    private final TicketAccessService ticketAccessService;

    private final TicketMapper ticketMapper;
    private final CommentMapper commentMapper;
    private final TicketEventMapper ticketEventMapper;

    @PostMapping
    public TicketResponse createTicket(@Valid @RequestBody CreateTicketRequest request) {
        Ticket ticket = ticketService.createTicket(
                request.getSectionId(),
                request.getServiceDirectionId(),
                request.getDescription()
        );

        return toTicketResponseWithSla(ticket);
    }

    @GetMapping("/{ticketId}")
    public TicketResponse getById(@PathVariable Long ticketId) {
        Ticket ticket = ticketService.getById(ticketId);

        return toTicketResponseWithSla(ticket);
    }

    @GetMapping
    public List<TicketResponse> getTicketsForCurrentUser(
            @RequestParam(required = false, defaultValue = "false") boolean hideResolved
    ) {
        List<Ticket> tickets = ticketService.getTicketsForCurrentUser(hideResolved);

        return tickets.stream()
                .map(this::toTicketResponseWithSla)
                .toList();
    }

    @PostMapping("/{ticketId}/take-in-work")
    public TicketResponse takeInWork(@PathVariable Long ticketId) {
        Ticket ticket = ticketService.takeInWork(ticketId);
        return toTicketResponseWithSla(ticket);
    }

    @PostMapping("/{ticketId}/return-to-dispatchers")
    public TicketResponse returnToDispatchers(
            @PathVariable Long ticketId,
            @Valid @RequestBody ReturnToDispatchersRequest request
    ) {
        Ticket ticket = ticketService.returnToDispatchers(ticketId, request.getPrivateComment());
        return toTicketResponseWithSla(ticket);
    }

    @PostMapping("/{ticketId}/resolve")
    public TicketResponse resolveTicket(
            @PathVariable Long ticketId,
            @Valid @RequestBody ResolveTicketRequest request
    ) {
        Ticket ticket = ticketService.resolveTicket(ticketId, request.getPublicComment());
        return toTicketResponseWithSla(ticket);
    }

    @PatchMapping("/{ticketId}/type")
    public TicketResponse changeTicketType(
            @PathVariable Long ticketId,
            @Valid @RequestBody ChangeTicketTypeRequest request
    ) {
        Ticket ticket = ticketService.changeTicketType(ticketId, request.getTicketTypeId());
        return toTicketResponseWithSla(ticket);
    }

    @PatchMapping("/{ticketId}/assignee")
    public TicketResponse assignEmployee(
            @PathVariable Long ticketId,
            @Valid @RequestBody AssignEmployeeRequest request
    ) {
        Ticket ticket = ticketService.assignEmployee(ticketId, request.getAssigneeId());
        return toTicketResponseWithSla(ticket);
    }

    @PostMapping("/{ticketId}/transfer")
    public TicketResponse transferToWorkGroup(
            @PathVariable Long ticketId,
            @Valid @RequestBody TransferToWorkGroupRequest request
    ) {
        Ticket ticket = ticketService.transferToWorkGroup(ticketId, request.getWorkGroupId());
        return toTicketResponseWithSla(ticket);
    }

    @GetMapping("/{ticketId}/comments")
    public List<CommentResponse> getComments(@PathVariable Long ticketId) {
        return commentService.getCommentsForCurrentUser(ticketId)
                .stream()
                .map(commentMapper::toResponse)
                .toList();
    }

    @PostMapping("/{ticketId}/comments/public")
    public CommentResponse addPublicComment(
            @PathVariable Long ticketId,
            @Valid @RequestBody AddPublicCommentRequest request
    ) {
        Comment comment = commentService.addPublicComment(ticketId, request.getContent());
        return commentMapper.toResponse(comment);
    }

    @PostMapping("/{ticketId}/comments/private")
    public CommentResponse addPrivateComment(
            @PathVariable Long ticketId,
            @Valid @RequestBody AddPrivateCommentRequest request
    ) {
        Comment comment = commentService.addPrivateComment(ticketId, request.getContent());
        return commentMapper.toResponse(comment);
    }

    @GetMapping("/{ticketId}/events")
    public List<TicketEventResponse> getEvents(@PathVariable Long ticketId) {
        return ticketEventService.getEventsForCurrentUser(ticketId)
                .stream()
                .map(ticketEventMapper::toResponse)
                .toList();
    }

    @PostMapping("/{ticketId}/cancel")
    public CommentResponse cancelTicket(@PathVariable Long ticketId) {
        Comment comment = commentService.addCancelComment(ticketId);
        return commentMapper.toResponse(comment);
    }

    private TicketResponse toTicketResponseWithSla(Ticket ticket) {
        TicketResponse response;

        if (ROLE_INITIATOR.equals(currentUserService.getCurrentRoleCode())) {
            response = ticketMapper.toInitiatorResponse(ticket);
        } else {
            response = ticketMapper.toResponse(ticket);
        }

        response.setSlaOverdue(slaService.isOverdue(ticket));

        TicketActionsResponse actions = TicketActionsResponse.builder()
                .canTakeInWork(ticketAccessService.canTakeInWork(ticket))
                .canResolve(ticketAccessService.canResolve(ticket))
                .canReturnToDispatchers(ticketAccessService.canReturnToDispatchers(ticket))
                .canAssignEmployee(ticketAccessService.canAssignEmployee(ticket))
                .canChangeTicketType(ticketAccessService.canChangeTicketType(ticket))
                .canAddPublicComment(ticketAccessService.canAddPublicComment(ticket))
                .canAddPrivateComment(ticketAccessService.canAddPrivateComment(ticket))
                .build();

        response.setAvailableActions(actions);

        return response;
    }
}