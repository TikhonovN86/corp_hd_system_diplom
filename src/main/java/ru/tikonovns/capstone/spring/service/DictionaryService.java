package ru.tikonovns.capstone.spring.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.tikonovns.capstone.spring.entity.*;
import ru.tikonovns.capstone.spring.repository.*;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DictionaryService {

    private final SectionRepository sectionRepository;
    private final ServiceDirectionRepository serviceDirectionRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final WorkGroupRepository workGroupRepository;
    private final UserRepository userRepository;

    public List<Section> getSections() {
        return sectionRepository.findAll();
    }

    public List<ServiceDirection> getServiceDirectionsBySectionId(Long sectionId) {
        return serviceDirectionRepository.findAllBySectionId(sectionId);
    }

    public List<TicketType> getTicketTypes() {
        return ticketTypeRepository.findAll();
    }

    public List<WorkGroup> getWorkGroups() {
        return workGroupRepository.findAll();
    }

    public List<User> getUsersByWorkGroupId(Long workGroupId) {
        return userRepository.findAllByWorkGroupId(workGroupId);
    }
}