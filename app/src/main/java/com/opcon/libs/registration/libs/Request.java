package com.opcon.libs.registration.libs;

import org.json.JSONObject;

/**
 * Created by Mahmut Taşkiran on 1/24/17.
 */

public interface Request {
    void executeSync(String domain, int port);
    boolean isSuccess();
    boolean isCompleted();
    String getException();
    JSONObject getBackoff();
}
