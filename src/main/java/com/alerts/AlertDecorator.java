package com.alerts;

public abstract class AlertDecorator {
    protected Alert decoratedAlert;

    public AlertDecorator(Alert alert) {
        this.decoratedAlert = alert;
    }

    public String getPatientId() {
        return decoratedAlert.getPatientId();
    }

    public String getCondition() {
        return decoratedAlert.getCondition();
    }

    public long getTimestamp() {
        return decoratedAlert.getTimestamp();
    }

}
