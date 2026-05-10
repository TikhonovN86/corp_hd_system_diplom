import { api } from './api.js';
import { ROLE, escapeHtml, formatDate } from './ui.js';

export async function renderEvents(ticketId, roleCode) {
    if (roleCode === ROLE.INITIATOR) return;

    const container = document.getElementById('eventsTab');
    if (!container) return;
    container.innerHTML = '<div class="ticket-meta">Загрузка журнала...</div>';

    try {
        const events = await api.getEvents(ticketId);
        if (!events.length) {
            container.innerHTML = '<div class="ticket-meta">Событий пока нет</div>';
            return;
        }

        container.innerHTML = events.map(event => `
            <div class="event">
                <div class="item-head">
                    <span>${escapeHtml(event.authorFullName || event.authorUsername || 'Система')}</span>
                    <span>${formatDate(event.createdAt)}</span>
                </div>
                <div class="item-body">${escapeHtml(event.eventMessage)}</div>
            </div>
        `).join('');
    } catch (error) {
        container.innerHTML = `<div class="alert alert-error">${escapeHtml(error.message)}</div>`;
    }
}
