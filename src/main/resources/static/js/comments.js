import { api } from './api.js';
import { escapeHtml, formatDate } from './ui.js';

export async function renderComments(ticketId) {
    const container = document.getElementById('commentsTab');
    if (!container) return;
    container.innerHTML = '<div class="ticket-meta">Загрузка комментариев...</div>';

    try {
        const comments = await api.getComments(ticketId);
        if (!comments.length) {
            container.innerHTML = '<div class="ticket-meta">Комментариев пока нет</div>';
            return;
        }

        container.innerHTML = comments.map(comment => `
            <div class="comment ${comment.isPrivate ? 'private' : ''}">
                <div class="item-head">
                    <span>${escapeHtml(comment.authorFullName || comment.authorUsername || 'Автор')}</span>
                    <span>${formatDate(comment.createdAt)}</span>
                </div>
                <div class="item-body">${escapeHtml(comment.content)}</div>
                ${comment.isPrivate ? '<div class="ticket-meta">Приватный комментарий</div>' : ''}
            </div>
        `).join('');
    } catch (error) {
        container.innerHTML = `<div class="alert alert-error">${escapeHtml(error.message)}</div>`;
    }
}
