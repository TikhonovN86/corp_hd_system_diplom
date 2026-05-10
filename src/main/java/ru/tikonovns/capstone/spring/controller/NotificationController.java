package ru.tikonovns.capstone.spring.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.tikonovns.capstone.spring.dto.response.NotificationResponse;
import ru.tikonovns.capstone.spring.dto.response.UnreadNotificationCountResponse;
import ru.tikonovns.capstone.spring.mapper.NotificationMapper;
import ru.tikonovns.capstone.spring.service.NotificationService;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationMapper notificationMapper;

    @GetMapping
    public List<NotificationResponse> getNotifications() {
        return notificationService.getNotificationsForCurrentUser()
                .stream()
                .map(notificationMapper::toResponse)
                .toList();
    }

    @GetMapping("/unread")
    public List<NotificationResponse> getUnreadNotifications() {
        return notificationService.getUnreadNotificationsForCurrentUser()
                .stream()
                .map(notificationMapper::toResponse)
                .toList();
    }

    @GetMapping("/unread/count")
    public UnreadNotificationCountResponse getUnreadCount() {
        return new UnreadNotificationCountResponse(
                notificationService.getUnreadCountForCurrentUser()
        );
    }

    @PostMapping("/{notificationId}/read")
    public void markAsRead(@PathVariable Long notificationId) {
        notificationService.markAsRead(notificationId);
    }
}