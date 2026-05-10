export async function request(url, options = {}) {
    const headers = options.body instanceof FormData ? {} : { 'Content-Type': 'application/json' };

    const response = await fetch(url, {
        credentials: 'same-origin',
        ...options,
        headers: {
            ...headers,
            ...(options.headers || {})
        }
    });

    if (response.redirected && response.url.includes('/login')) {
        window.location.href = '/login.html';
        return;
    }

    if (!response.ok) {
        let message = `Ошибка ${response.status}`;
        try {
            const error = await response.json();
            message = error.message || message;
        } catch (_) {}
        throw new Error(message);
    }

    if (response.status === 204) return null;

    const text = await response.text();
    return text ? JSON.parse(text) : null;
}

export const api = {
    me: () => request('/api/me'),

    getTickets: (hideResolved = false) => request(`/api/tickets?hideResolved=${hideResolved}`),
    createTicket: (payload) => request('/api/tickets', { method: 'POST', body: JSON.stringify(payload) }),
    getTicket: (ticketId) => request(`/api/tickets/${ticketId}`),
    takeInWork: (ticketId) => request(`/api/tickets/${ticketId}/take-in-work`, { method: 'POST' }),
    transferToWorkGroup: (ticketId, workGroupId) => request(`/api/tickets/${ticketId}/transfer`, { method: 'POST', body: JSON.stringify({ workGroupId }) }),
    returnToDispatchers: (ticketId, privateComment) => request(`/api/tickets/${ticketId}/return-to-dispatchers`, { method: 'POST', body: JSON.stringify({ privateComment }) }),
    resolveTicket: (ticketId, publicComment) => request(`/api/tickets/${ticketId}/resolve`, { method: 'POST', body: JSON.stringify({ publicComment }) }),
    changeTicketType: (ticketId, ticketTypeId) => request(`/api/tickets/${ticketId}/type`, { method: 'PATCH', body: JSON.stringify({ ticketTypeId }) }),
    assignEmployee: (ticketId, assigneeId) => request(`/api/tickets/${ticketId}/assignee`, { method: 'PATCH', body: JSON.stringify({ assigneeId }) }),
    cancelTicket: (ticketId) => request(`/api/tickets/${ticketId}/cancel`, { method: 'POST' }),

    getComments: (ticketId) => request(`/api/tickets/${ticketId}/comments`),
    addPublicComment: (ticketId, content) => request(`/api/tickets/${ticketId}/comments/public`, { method: 'POST', body: JSON.stringify({ content }) }),
    addPrivateComment: (ticketId, content) => request(`/api/tickets/${ticketId}/comments/private`, { method: 'POST', body: JSON.stringify({ content }) }),
    getEvents: (ticketId) => request(`/api/tickets/${ticketId}/events`),

    getNotifications: () => request('/api/notifications'),
    getUnreadCount: () => request('/api/notifications/unread/count'),
    markNotificationRead: (notificationId) => request(`/api/notifications/${notificationId}/read`, { method: 'POST' }),

    getSections: () => request('/api/dictionaries/sections'),
    getServiceDirections: (sectionId) => request(`/api/dictionaries/sections/${sectionId}/service-directions`),
    getTicketTypes: () => request('/api/dictionaries/ticket-types'),
    getWorkGroups: () => request('/api/dictionaries/work-groups'),
    getUsersByWorkGroup: (workGroupId) => request(`/api/dictionaries/work-groups/${workGroupId}/users`)
};
