import { api } from './api.js';
import { state } from './app.js';
import { ROLE, escapeHtml, formatDate, openModal, showToast } from './ui.js';

export async function initNotifications() {
    if (state.currentUser?.roleCode !== ROLE.INITIATOR) return;
    document.getElementById('notificationsButton').addEventListener('click', openNotificationsModal);
    await refreshNotificationBadge();
}

export async function refreshNotificationBadge() {
    if (state.currentUser?.roleCode !== ROLE.INITIATOR) return;

    try {
        const result = await api.getUnreadCount();
        const badge = document.getElementById('notificationBadge');
        const count = result?.count || 0;
        badge.textContent = String(count);
        badge.classList.toggle('hidden', count === 0);
    } catch (_) {}
}

async function openNotificationsModal() {
    try {
        const notifications = await api.getNotifications();
        openModal('Уведомления', buildNotificationsHtml(notifications));
        document.querySelectorAll('[data-notification-id]').forEach(button => {
            button.addEventListener('click', async () => {
                try {
                    await api.markNotificationRead(Number(button.dataset.notificationId));
                    showToast('Уведомление прочитано');
                    await refreshNotificationBadge();
                    await openNotificationsModal();
                } catch (error) { showToast(error.message); }
            });
        });
    } catch (error) {
        showToast(error.message);
    }
}

function buildNotificationsHtml(notifications) {
    if (!notifications.length) return '<div class="ticket-meta">Уведомлений нет</div>';

    return notifications.map(item => `
        <div class="notification">
            <div class="item-head">
                <strong>${escapeHtml(item.title)}</strong>
                <span>${formatDate(item.createdAt)}</span>
            </div>
            <div class="item-body">${escapeHtml(item.message)}</div>
            <div class="ticket-meta">${escapeHtml(item.ticketNumber || '')}</div>
            ${item.isRead ? '<div class="ticket-meta">Прочитано</div>' : `<button class="secondary-button" data-notification-id="${item.id}" type="button">Отметить прочитанным</button>`}
        </div>
    `).join('');
}
