import { api } from './api.js';
import { ROLE, closeModal, showToast } from './ui.js';
import { loadTickets, renderTicketCard, selectedTicketId } from './tickets.js';
import { openCreateTicketModal } from './ticketForms.js';
import { initNotifications, refreshNotificationBadge } from './notifications.js';

export const state = {
    currentUser: null,
    tickets: [],
    dictionaries: {
        sections: [],
        serviceDirections: [],
        ticketTypes: [],
        workGroups: [],
        usersByWorkGroup: new Map()
    }
};

async function init() {
    bindGlobalEvents();

    try {
        state.currentUser = await api.me();
        renderUserInfo();
        configureRoleUi();
        await loadTickets();
        await initNotifications();
    } catch (error) {
        showToast(error.message);
    }
}

function renderUserInfo() {
    const user = state.currentUser;
    document.getElementById('userInfo').textContent = `${user.fullName || user.username} • ${roleLabel(user.roleCode)}`;
}

function configureRoleUi() {
    if (state.currentUser.roleCode === ROLE.INITIATOR) {
        document.getElementById('createTicketOpenButton').classList.remove('hidden');
        document.getElementById('notificationsButton').classList.remove('hidden');
    }
}

function roleLabel(roleCode) {
    switch (roleCode) {
        case ROLE.INITIATOR: return 'Инициатор';
        case ROLE.DISPATCHER: return 'Диспетчер';
        case ROLE.EXECUTOR: return 'Исполнитель';
        default: return roleCode || 'Пользователь';
    }
}

function bindGlobalEvents() {
    document.getElementById('hideResolvedCheckbox').addEventListener('change', () => loadTickets());
    document.getElementById('refreshTicketsButton').addEventListener('click', () => loadTickets());
    document.getElementById('createTicketOpenButton').addEventListener('click', () => openCreateTicketModal());
    document.getElementById('modalCloseButton').addEventListener('click', closeModal);
    document.getElementById('modalOverlay').addEventListener('click', (event) => {
        if (event.target.id === 'modalOverlay') closeModal();
    });

    document.addEventListener('ticket-updated', async (event) => {
        await loadTickets();
        const ticketId = event.detail?.ticketId || selectedTicketId;
        if (ticketId) await renderTicketCard(ticketId);
        await refreshNotificationBadge();
    });
}

init();
