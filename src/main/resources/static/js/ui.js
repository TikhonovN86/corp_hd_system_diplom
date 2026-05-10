export const ROLE = {
    INITIATOR: 'ROLE_INITIATOR',
    DISPATCHER: 'ROLE_DISPATCHER',
    EXECUTOR: 'ROLE_EXECUTOR'
};

export const STATUS_LABELS = {
    NEW: 'Новое',
    IN_PROGRESS: 'В работе',
    UNASSIGNED: 'На распределении',
    RESOLVED: 'Решено'
};

export function formatDate(value) {
    if (!value) return '—';
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return value;
    return date.toLocaleString('ru-RU', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

export function statusLabel(status) {
    return STATUS_LABELS[status] || status || '—';
}

export function escapeHtml(value) {
    return String(value ?? '')
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;')
        .replaceAll("'", '&#039;');
}

export function showToast(message) {
    const toast = document.getElementById('toast');
    toast.textContent = message;
    toast.classList.remove('hidden');
    setTimeout(() => toast.classList.add('hidden'), 2800);
}

export function openModal(title, html) {
    document.getElementById('modalTitle').textContent = title;
    document.getElementById('modalBody').innerHTML = html;
    document.getElementById('modalOverlay').classList.remove('hidden');
}

export function closeModal() {
    document.getElementById('modalOverlay').classList.add('hidden');
    document.getElementById('modalBody').innerHTML = '';
}

export function getFormValue(form, name) {
    const value = new FormData(form).get(name);
    return typeof value === 'string' ? value.trim() : value;
}
