import { api } from './api.js';
import { state } from './app.js';

export async function ensureSections() {
    if (!state.dictionaries.sections.length) {
        state.dictionaries.sections = await api.getSections();
    }
    return state.dictionaries.sections;
}

export async function loadServiceDirections(sectionId) {
    state.dictionaries.serviceDirections = await api.getServiceDirections(sectionId);
    return state.dictionaries.serviceDirections;
}

export async function ensureTicketTypes() {
    if (!state.dictionaries.ticketTypes.length) {
        state.dictionaries.ticketTypes = await api.getTicketTypes();
    }
    return state.dictionaries.ticketTypes;
}

export async function ensureWorkGroups() {
    if (!state.dictionaries.workGroups.length) {
        state.dictionaries.workGroups = await api.getWorkGroups();
    }
    return state.dictionaries.workGroups;
}

export async function ensureUsersByWorkGroup(workGroupId) {
    if (!workGroupId) return [];
    if (!state.dictionaries.usersByWorkGroup.has(workGroupId)) {
        const users = await api.getUsersByWorkGroup(workGroupId);
        state.dictionaries.usersByWorkGroup.set(workGroupId, users);
    }
    return state.dictionaries.usersByWorkGroup.get(workGroupId);
}
