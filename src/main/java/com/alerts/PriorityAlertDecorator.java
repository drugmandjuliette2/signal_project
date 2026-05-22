package com.alerts;

/**
 * decorator that adds a priority level to an exisitng alert
 */

public class PriorityAlertDecorator extends AlertDecorator {
    private String priority;

    public PriorityAlertDecorator(Alert alert, String priority) {
        super(alert);
        this.priority = priority;
    }

    public String getPriority() {
        return this.priority;
    }

    @Override
    public String getCondition() {
        return "[" + priority + " PRIORITY] " + super.getCondition();
    }
}
