package ru.tikonovns.capstone.spring.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.tikonovns.capstone.spring.dto.response.dictionary.DictionaryCodeResponse;
import ru.tikonovns.capstone.spring.dto.response.dictionary.DictionaryItemResponse;
import ru.tikonovns.capstone.spring.dto.response.dictionary.UserDictionaryResponse;
import ru.tikonovns.capstone.spring.mapper.DictionaryMapper;
import ru.tikonovns.capstone.spring.service.DictionaryService;

import java.util.List;

import static ru.tikonovns.capstone.spring.utils.constants.TicketStatus.*;

@RestController
@RequestMapping("/api/dictionaries")
@RequiredArgsConstructor
public class DictionaryController {

    private final DictionaryService dictionaryService;
    private final DictionaryMapper dictionaryMapper;

    @GetMapping("/sections")
    @PreAuthorize("hasAnyAuthority('ROLE_INITIATOR', 'ROLE_DISPATCHER', 'ROLE_EXECUTOR')")
    public List<DictionaryItemResponse> getSections() {
        return dictionaryService.getSections()
                .stream()
                .map(dictionaryMapper::sectionToResponse)
                .toList();
    }

    @GetMapping("/sections/{sectionId}/service-directions")
    @PreAuthorize("hasAnyAuthority('ROLE_INITIATOR', 'ROLE_DISPATCHER', 'ROLE_EXECUTOR')")
    public List<DictionaryItemResponse> getServiceDirectionsBySection(
            @PathVariable Long sectionId
    ) {
        return dictionaryService.getServiceDirectionsBySectionId(sectionId)
                .stream()
                .map(dictionaryMapper::serviceDirectionToResponse)
                .toList();
    }

    @GetMapping("/ticket-types")
    @PreAuthorize("hasAnyAuthority('ROLE_DISPATCHER', 'ROLE_EXECUTOR')")
    public List<DictionaryItemResponse> getTicketTypes() {
        return dictionaryService.getTicketTypes()
                .stream()
                .map(dictionaryMapper::ticketTypeToResponse)
                .toList();
    }

    @GetMapping("/work-groups")
    @PreAuthorize("hasAuthority('ROLE_DISPATCHER')")
    public List<DictionaryItemResponse> getWorkGroups() {
        return dictionaryService.getWorkGroups()
                .stream()
                .map(dictionaryMapper::workGroupToResponse)
                .toList();
    }

    @GetMapping("/work-groups/{workGroupId}/users")
    @PreAuthorize("hasAnyAuthority('ROLE_DISPATCHER', 'ROLE_EXECUTOR')")
    public List<UserDictionaryResponse> getUsersByWorkGroup(
            @PathVariable Long workGroupId
    ) {
        return dictionaryService.getUsersByWorkGroupId(workGroupId)
                .stream()
                .map(dictionaryMapper::userToDictionaryResponse)
                .toList();
    }

    @GetMapping("/ticket-statuses")
    @PreAuthorize("hasAnyAuthority('ROLE_DISPATCHER', 'ROLE_EXECUTOR')")
    public List<DictionaryCodeResponse> getTicketStatuses() {
        return List.of(
                new DictionaryCodeResponse(STATUS_NEW, "Новое"),
                new DictionaryCodeResponse(STATUS_IN_PROGRESS, "В работе"),
                new DictionaryCodeResponse(STATUS_UNASSIGNED, "На распределении"),
                new DictionaryCodeResponse(STATUS_RESOLVED, "Решено")
        );
    }
}