import { api } from './api.js';
import { state } from './app.js';
import { ROLE, escapeHtml, formatDate, statusLabel, showToast } from './ui.js';
import { renderComments } from './comments.js';
import { renderEvents } from './events.js';
import { openActionModal } from './ticketForms.js';

export let selectedTicketId = null;

export async function loadTickets() {
    const hideResolved = document.getElementById('hideResolvedCheckbox').checked;
    const list = document.getElementById('ticketList');
    list.innerHTML = '<div class="ticket-meta">Загрузка...</div>';

    try {
        state.tickets = await api.getTickets(hideResolved);
        renderTicketList();
    } catch (error) {
        list.innerHTML = `<div class="alert alert-error">${escapeHtml(error.message)}</div>`;
    }
}

function renderTicketList() {
    const list = document.getElementById('ticketList');

    if (!state.tickets.length) {
        list.innerHTML = '<div class="ticket-meta">Обращения не найдены</div>';
        return;
    }

    // list.innerHTML = state.tickets.map(ticket => `
    //     <div class="ticket-item ${selectedTicketId === ticket.id ? 'active' : ''}" data-ticket-id="${ticket.id}">
    //         <div class="ticket-row">
    //             <div class="ticket-number">${escapeHtml(ticket.ticketNumber || `#${ticket.id}`)}</div>
    //             <span class="status ${escapeHtml(ticket.status)}">${escapeHtml(statusLabel(ticket.status))}</span>
    //         </div>
    //         <div class="ticket-meta">${escapeHtml(ticket.serviceDirectionName || 'Без направления')}</div>
    //         <div class="ticket-meta">Создано: ${formatDate(ticket.createdAt)}</div>
    //         <div class="ticket-meta">SLA: ${ticket.slaOverdue ? 'Просрочено' : 'Не просрочено'}</div>
    //     </div>
    // `).join('');

    // Актуальный боковой список тикетов.
    list.innerHTML = state.tickets.map(ticket => {
        const assignee = ticket.assigneeFullName || ticket.assigneeUsername || 'Не назначен';

        return `
        <div class="ticket-item ${selectedTicketId === ticket.id ? 'active' : ''}" data-ticket-id="${ticket.id}">
            <div class="ticket-row">
                <div class="ticket-number">${escapeHtml(ticket.ticketNumber || `#${ticket.id}`)}</div>
                <span class="status ${escapeHtml(ticket.status)}">${escapeHtml(statusLabel(ticket.status))}</span>
            </div>
            <div class="ticket-meta">${escapeHtml(ticket.serviceDirectionName || 'Без направления')}</div>
            <div class="ticket-meta">Создано: ${formatDate(ticket.createdAt)}</div>
            <div class="ticket-meta">SLA: ${ticket.slaOverdue ? 'Просрочено' : 'Не просрочено'}</div>
            <div class="ticket-meta">Ответственный: <strong>${escapeHtml(assignee)}</strong></div>
        </div>
    `;
    }).join('');

    list.querySelectorAll('.ticket-item').forEach(item => {
        item.addEventListener('click', () => renderTicketCard(Number(item.dataset.ticketId)));
    });
}

export async function renderTicketCard(ticketId) {
    selectedTicketId = ticketId;
    renderTicketList();

    const emptyState = document.getElementById('emptyState');
    const card = document.getElementById('ticketCard');
    emptyState.classList.add('hidden');
    card.classList.remove('hidden');
    card.innerHTML = '<div class="ticket-meta">Загрузка карточки...</div>';

    try {
        const ticket = await api.getTicket(ticketId);
        card.innerHTML = buildTicketCardHtml(ticket);
        bindTicketCardEvents(ticket);
        await renderComments(ticket.id);
        await renderEvents(ticket.id, state.currentUser.roleCode);
    } catch (error) {
        card.innerHTML = `<div class="alert alert-error">${escapeHtml(error.message)}</div>`;
    }
}

function buildTicketCardHtml(ticket) {
    const canSeeInternalFields = state.currentUser.roleCode !== ROLE.INITIATOR;

    return `
        <div class="card-header">
            <div>
                <div class="card-title">${escapeHtml(ticket.ticketNumber || `Обращение #${ticket.id}`)}</div>
                <div class="card-subtitle">${escapeHtml(ticket.serviceDirectionName || '')}</div>
            </div>
            <span class="status ${escapeHtml(ticket.status)}">${escapeHtml(statusLabel(ticket.status))}</span>
        </div>

        <div class="card-grid">
            <div class="field"><div class="field-label">Раздел</div><div class="field-value">${escapeHtml(ticket.sectionName)}</div></div>
            <div class="field"><div class="field-label">Направление</div><div class="field-value">${escapeHtml(ticket.serviceDirectionName)}</div></div>
            ${canSeeInternalFields ? `<div class="field"><div class="field-label">Тип обращения</div><div class="field-value">${escapeHtml(ticket.ticketTypeName)}</div></div>` : ''}
            <div class="field"><div class="field-label">Дата регистрации</div><div class="field-value">${formatDate(ticket.createdAt)}</div></div>
            <div class="field"><div class="field-label">Крайний срок</div><div class="field-value">${formatDate(ticket.deadline)}</div></div>
            <div class="field"><div class="field-label">SLA</div><div class="field-value">${ticket.slaOverdue ? 'Просрочено' : 'Не просрочено'}</div></div>
            ${canSeeInternalFields ? `<div class="field"><div class="field-label">Рабочая группа</div><div class="field-value">${escapeHtml(ticket.workGroupName || ticket.workGroupCode)}</div></div>` : ''}
            ${canSeeInternalFields ? `<div class="field"><div class="field-label">Ответственный</div><div class="field-value">${escapeHtml(ticket.assigneeFullName || ticket.assigneeUsername || '—')}</div></div>` : ''}
            ${canSeeInternalFields ? `<div class="field"><div class="field-label">Инициатор</div><div class="field-value">${escapeHtml(ticket.initiatorFullName || ticket.initiatorUsername)}</div></div>` : ''}
        </div>

        <div class="field-label">Детальное описание</div>
        <div class="description-box">${escapeHtml(ticket.description || '—')}</div>

        <div class="actions">${buildActionsHtml(ticket)}</div>

        <div class="tabs">
            <button class="tab-button active" data-tab="comments" type="button">Комментарии</button>
            ${canSeeInternalFields ? '<button class="tab-button" data-tab="events" type="button">Журнал</button>' : ''}
        </div>

        <div id="commentsTab"></div>
        <div id="eventsTab" class="hidden"></div>
    `;
}

function buildActionsHtml(ticket) {
    const actions = ticket.availableActions || {};
    const buttons = [];

    if (actions.canTakeInWork) buttons.push('<button class="primary-button" data-action="take" type="button">В работу</button>');
    if (actions.canResolve) buttons.push('<button class="primary-button" data-action="resolve" type="button">Выполнить</button>');
    if (actions.canReturnToDispatchers) buttons.push('<button class="secondary-button" data-action="return" type="button">Вернуть диспетчерам</button>');
    if (state.currentUser.roleCode === ROLE.DISPATCHER) buttons.push('<button class="secondary-button" data-action="transfer" type="button">Передать обращение</button>');
    if (actions.canChangeTicketType) buttons.push('<button class="secondary-button" data-action="type" type="button">Изменить тип</button>');
    if (actions.canAssignEmployee) buttons.push('<button class="secondary-button" data-action="assignee" type="button">Назначить ответственного</button>');
    if (actions.canAddPublicComment) buttons.push('<button class="secondary-button" data-action="public-comment" type="button">Добавить комментарий</button>');
    if (actions.canAddPrivateComment) buttons.push('<button class="secondary-button" data-action="private-comment" type="button">Приватный комментарий</button>');
    if (state.currentUser.roleCode === ROLE.INITIATOR && ticket.status !== 'RESOLVED') buttons.push('<button class="danger-button" data-action="cancel" type="button">Отменить обращение</button>');

    return buttons.length ? buttons.join('') : '<span class="ticket-meta">Нет доступных действий</span>';
}

function bindTicketCardEvents(ticket) {
    document.querySelectorAll('[data-action]').forEach(button => {
        button.addEventListener('click', async () => {
            const action = button.dataset.action;
            if (action === 'take') {
                try {
                    await api.takeInWork(ticket.id);
                    showToast('Обращение взято в работу');
                    document.dispatchEvent(new CustomEvent('ticket-updated', { detail: { ticketId: ticket.id } }));
                } catch (error) { showToast(error.message); }
                return;
            }
            if (action === 'cancel') {
                if (!confirm('Добавить комментарий об отмене обращения?')) return;
                try {
                    await api.cancelTicket(ticket.id);
                    showToast('Комментарий об отмене добавлен');
                    document.dispatchEvent(new CustomEvent('ticket-updated', { detail: { ticketId: ticket.id } }));
                } catch (error) { showToast(error.message); }
                return;
            }
            openActionModal(action, ticket);
        });
    });

    document.querySelectorAll('.tab-button').forEach(button => {
        button.addEventListener('click', () => {
            document.querySelectorAll('.tab-button').forEach(b => b.classList.remove('active'));
            button.classList.add('active');
            document.getElementById('commentsTab').classList.toggle('hidden', button.dataset.tab !== 'comments');
            const eventsTab = document.getElementById('eventsTab');
            if (eventsTab) eventsTab.classList.toggle('hidden', button.dataset.tab !== 'events');
        });
    });
}
