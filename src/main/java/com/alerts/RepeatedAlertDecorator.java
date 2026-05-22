package com.alerts;

public class RepeatedAlertDecorator extends AlertDecorator {

    private int repeatIntervalSeconds;

    public RepeatedAlertDecorator(Alert alert, int repeatIntervalSeconds) {
        super(alert);
        this.repeatIntervalSeconds = repeatIntervalSeconds;
    }

    public int getRepeatIntervalSeconds() {
        return this.repeatIntervalSeconds;
    }

    @Override
    public String getCondition() {
        return super.getCondition() + " (Repeats every " + repeatIntervalSeconds + ")";
    }
}
