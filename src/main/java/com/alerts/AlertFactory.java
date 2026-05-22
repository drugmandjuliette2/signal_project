package com.alerts;
/*
* the base factory to generate alerts
*/

public abstract class AlertFactory {

    public abstract Alert createAlert(String patientId, String condition, long timestamp);

}
