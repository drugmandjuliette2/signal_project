package com.alerts;

import com.data_management.Patient;

/**
 * interface to represent monitoring algorithm strategy
 */

public interface AlertStrategy {

    void checkAlert(Patient patient);

}
