package com.opcon.libs.registration.libs;

import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import timber.log.Timber;


/**
 * Created by Mahmut Taşkiran on 1/24/17.
 */

public class TokenRequest implements Request {

    private String phone;
    private String dial_code;
    private String locale;
    private String method;

    private boolean completed;
    private boolean success;

    private String exception;

    private JSONObject backoff;

    public TokenRequest(String phone, String dial_code, String locale, @Nullable String method) {
        this.phone = phone;
        this.dial_code = dial_code;
        this.locale = locale;
        this.method = method;
        if (this.method == null) {
            this.method = "sms";
        }
    }

    @Override
    public void executeSync(String domain, int port) {
        OkHttpClient mClient = new OkHttpClient();
        HttpUrl.Builder mUrlBuilder = HttpUrl.parse(domain + ":" + port + File.separator + "request").newBuilder();

        mUrlBuilder.addQueryParameter("phone", phone);
        mUrlBuilder.addQueryParameter("dial_code", dial_code);
        mUrlBuilder.addQueryParameter("locale", locale);
        mUrlBuilder.addQueryParameter("method", method);


        String mUrl = mUrlBuilder.build().toString();

        okhttp3.Request mRequest =  new okhttp3.Request.Builder()
                .url(mUrl)
                .get()
                .build();

        try {
            Response response = mClient.newCall(mRequest).execute();
            String body = response.body().string();

            Timber.d(body);

            completed = body != null;

            if (completed) {
                success = body.equals("success");
            }

            if (!success) {
                if (body != null && (body.equals("bad_request") || body.equals("failed"))) {
                    exception = body;
                } else {
                    try {
                        backoff = new JSONObject(body);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        completed = false;
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            completed = false;
            success = false;
        }
    }

    public boolean isCompleted() {
        return completed;
    }

    public String getException() {
        return exception;
    }

    public boolean isSuccess() {
        return success && completed;
    }

    public JSONObject getBackoff() {
        return backoff;
    }
}
