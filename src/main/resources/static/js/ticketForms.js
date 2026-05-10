import { api } from './api.js';
import { closeModal, escapeHtml, getFormValue, openModal, showToast } from './ui.js';
import { ensureSections, loadServiceDirections, ensureTicketTypes, ensureUsersByWorkGroup, ensureWorkGroups } from './dictionaries.js';

export async function openCreateTicketModal() {
    try {
        const sections = await ensureSections();
        openModal('Новое обращение', `
            <form id="createTicketForm" class="form-grid">
                <div class="form-row">
                    <label>Раздел</label>
                    <select name="sectionId" required>
                        <option value="">Выберите раздел</option>
                        ${sections.map(item => `<option value="${item.id}">${escapeHtml(item.name)}</option>`).join('')}
                    </select>
                </div>
                <div class="form-row">
                    <label>Направление обслуживания</label>
                    <select name="serviceDirectionId" required disabled>
                        <option value="">Сначала выберите раздел</option>
                    </select>
                </div>
                <div id="serviceDirectionDescription" class="help-text hidden"></div>
                <div class="form-row">
                    <label>Детальное описание</label>
                    <textarea name="description" required></textarea>
                </div>
                <button class="primary-button" type="submit">Подать обращение</button>
            </form>
        `);

        const form = document.getElementById('createTicketForm');
        const sectionSelect = form.elements.sectionId;
        const directionSelect = form.elements.serviceDirectionId;
        const descriptionBox = document.getElementById('serviceDirectionDescription');

        sectionSelect.addEventListener('change', async () => {
            const sectionId = sectionSelect.value;
            directionSelect.disabled = true;
            directionSelect.innerHTML = '<option value="">Загрузка...</option>';
            descriptionBox.classList.add('hidden');
            descriptionBox.textContent = '';

            if (!sectionId) {
                directionSelect.innerHTML = '<option value="">Сначала выберите раздел</option>';
                return;
            }

            const directions = await loadServiceDirections(sectionId);
            directionSelect.innerHTML = '<option value="">Выберите направление</option>' +
                directions.map(item => `<option value="${item.id}" data-description="${escapeHtml(item.description || '')}">${escapeHtml(item.name)}</option>`).join('');
            directionSelect.disabled = false;
        });

        directionSelect.addEventListener('change', () => {
            const option = directionSelect.selectedOptions[0];
            const description = option?.dataset?.description || '';
            descriptionBox.textContent = description;
            descriptionBox.classList.toggle('hidden', !description);
        });

        form.addEventListener('submit', async (event) => {
            event.preventDefault();
            try {
                const payload = {
                    sectionId: Number(getFormValue(form, 'sectionId')),
                    serviceDirectionId: Number(getFormValue(form, 'serviceDirectionId')),
                    description: getFormValue(form, 'description')
                };
                const created = await api.createTicket(payload);
                closeModal();
                showToast('Обращение создано');
                document.dispatchEvent(new CustomEvent('ticket-updated', { detail: { ticketId: created.id } }));
            } catch (error) { showToast(error.message); }
        });
    } catch (error) {
        showToast(error.message);
    }
}

export async function openActionModal(action, ticket) {
    if (action === 'public-comment') return openCommentModal(ticket, false);
    if (action === 'private-comment') return openCommentModal(ticket, true);
    if (action === 'resolve') return openResolveModal(ticket);
    if (action === 'return') return openReturnModal(ticket);
    if (action === 'type') return openTypeModal(ticket);
    if (action === 'transfer') return openTransferModal(ticket);
    if (action === 'assignee') return openAssigneeModal(ticket);
}

function openCommentModal(ticket, isPrivate) {
    openModal(isPrivate ? 'Приватный комментарий' : 'Добавить комментарий', `
        <form id="commentForm" class="form-grid">
            <div class="form-row">
                <label>Комментарий</label>
                <textarea name="content" required></textarea>
            </div>
            <button class="primary-button" type="submit">Добавить</button>
        </form>
    `);

    document.getElementById('commentForm').addEventListener('submit', async (event) => {
        event.preventDefault();
        try {
            const content = getFormValue(event.currentTarget, 'content');
            if (isPrivate) await api.addPrivateComment(ticket.id, content);
            else await api.addPublicComment(ticket.id, content);
            closeModal();
            showToast('Комментарий добавлен');
            document.dispatchEvent(new CustomEvent('ticket-updated', { detail: { ticketId: ticket.id } }));
        } catch (error) { showToast(error.message); }
    });
}

function openResolveModal(ticket) {
    openModal('Выполнить обращение', `
        <form id="resolveForm" class="form-grid">
            <div class="form-row">
                <label>Публичный комментарий о решении</label>
                <textarea name="publicComment" required></textarea>
            </div>
            <button class="primary-button" type="submit">Выполнить</button>
        </form>
    `);

    document.getElementById('resolveForm').addEventListener('submit', async (event) => {
        event.preventDefault();
        try {
            await api.resolveTicket(ticket.id, getFormValue(event.currentTarget, 'publicComment'));
            closeModal();
            showToast('Обращение решено');
            document.dispatchEvent(new CustomEvent('ticket-updated', { detail: { ticketId: ticket.id } }));
        } catch (error) { showToast(error.message); }
    });
}

function openReturnModal(ticket) {
    openModal('Вернуть на диспетчеров', `
        <form id="returnForm" class="form-grid">
            <div class="form-row">
                <label>Причина возврата — приватный комментарий</label>
                <textarea name="privateComment" required></textarea>
            </div>
            <button class="primary-button" type="submit">Вернуть</button>
        </form>
    `);

    document.getElementById('returnForm').addEventListener('submit', async (event) => {
        event.preventDefault();
        try {
            await api.returnToDispatchers(ticket.id, getFormValue(event.currentTarget, 'privateComment'));
            closeModal();
            showToast('Обращение возвращено диспетчерам');
            document.dispatchEvent(new CustomEvent('ticket-updated', { detail: { ticketId: ticket.id } }));
        } catch (error) { showToast(error.message); }
    });
}

async function openTypeModal(ticket) {
    try {
        const types = await ensureTicketTypes();
        openModal('Изменить тип обращения', `
            <form id="typeForm" class="form-grid">
                <div class="form-row">
                    <label>Тип обращения</label>
                    <select name="ticketTypeId" required>
                        ${types.map(item => `<option value="${item.id}" ${item.id === ticket.ticketTypeId ? 'selected' : ''}>${escapeHtml(item.name)}</option>`).join('')}
                    </select>
                </div>
                <button class="primary-button" type="submit">Сохранить</button>
            </form>
        `);

        document.getElementById('typeForm').addEventListener('submit', async (event) => {
            event.preventDefault();
            try {
                await api.changeTicketType(ticket.id, Number(getFormValue(event.currentTarget, 'ticketTypeId')));
                closeModal();
                showToast('Тип обращения изменён');
                document.dispatchEvent(new CustomEvent('ticket-updated', { detail: { ticketId: ticket.id } }));
            } catch (error) { showToast(error.message); }
        });
    } catch (error) { showToast(error.message); }
}

async function openTransferModal(ticket) {
    try {
        const groups = await ensureWorkGroups();
        openModal('Передать обращение', `
            <form id="transferForm" class="form-grid">
                <div class="form-row">
                    <label>Рабочая группа</label>
                    <select name="workGroupId" required>
                        <option value="">Выберите рабочую группу</option>
                        ${groups.map(item => `<option value="${item.id}">${escapeHtml(item.name || item.code)}</option>`).join('')}
                    </select>
                </div>
                <button class="primary-button" type="submit">Передать</button>
            </form>
        `);

        document.getElementById('transferForm').addEventListener('submit', async (event) => {
            event.preventDefault();
            try {
                await api.transferToWorkGroup(ticket.id, Number(getFormValue(event.currentTarget, 'workGroupId')));
                closeModal();
                showToast('Обращение передано');
                document.dispatchEvent(new CustomEvent('ticket-updated', { detail: { ticketId: ticket.id } }));
            } catch (error) { showToast(error.message); }
        });
    } catch (error) { showToast(error.message); }
}

async function openAssigneeModal(ticket) {
    try {
        const users = await ensureUsersByWorkGroup(ticket.workGroupId);
        openModal('Назначить ответственного', `
            <form id="assigneeForm" class="form-grid">
                <div class="form-row">
                    <label>Ответственный сотрудник</label>
                    <select name="assigneeId" required>
                        <option value="">Выберите сотрудника</option>
                        ${users.map(user => `<option value="${user.id}" ${user.id === ticket.assigneeId ? 'selected' : ''}>${escapeHtml(user.fullName || user.username)}</option>`).join('')}
                    </select>
                </div>
                <button class="primary-button" type="submit">Сохранить</button>
            </form>
        `);

        document.getElementById('assigneeForm').addEventListener('submit', async (event) => {
            event.preventDefault();
            try {
                await api.assignEmployee(ticket.id, Number(getFormValue(event.currentTarget, 'assigneeId')));
                closeModal();
                showToast('Ответственный назначен');
                document.dispatchEvent(new CustomEvent('ticket-updated', { detail: { ticketId: ticket.id } }));
            } catch (error) { showToast(error.message); }
        });
    } catch (error) { showToast(error.message); }
}
