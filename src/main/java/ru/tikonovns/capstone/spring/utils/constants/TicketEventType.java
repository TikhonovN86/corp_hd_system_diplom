package ru.tikonovns.capstone.spring.utils.constants;

public final class TicketEventType {

    private TicketEventType() {
    }

    public static final String CREATED = "CREATED";
    public static final String STATUS_CHANGED = "STATUS_CHANGED";
    public static final String GROUP_ASSIGNED = "GROUP_ASSIGNED";
    public static final String COMMENT_ADDED = "COMMENT_ADDED";
    public static final String PRIVATE_COMMENT_ADDED = "PRIVATE_COMMENT_ADDED";
    public static final String TYPE_CHANGED = "TYPE_CHANGED";
    public static final String SLA_RECALCULATED = "SLA_RECALCULATED";
    public static final String EMPLOYEE_ASSIGNED = "EMPLOYEE_ASSIGNED";
}