package com.opcon.libs.registration.libs;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Response;

/**
 * Created by Mahmut Taşkiran on
 * 30/01/2017.
 */

public class TestTokenRequest implements Request {

    private String phone;
    private String dial_code;
    private String locale;

    private String email;
    private String password;
    private boolean completed;
    private boolean success;

    public String getEmail() {
        return email;
    }

    public TestTokenRequest(String phone, String dial_code, String locale) {
        this.phone = phone;
        this.dial_code = dial_code;
        this.locale = locale;
    }

    @Override
    public void executeSync(String domain, int port) {
        OkHttpClient mClient = new OkHttpClient();

        HttpUrl.Builder mUrlBuilder = HttpUrl.parse(domain + ":" + port + File.separator + "test").newBuilder();



        mUrlBuilder.addQueryParameter("phone", phone);
        mUrlBuilder.addQueryParameter("dial_code", dial_code);
        mUrlBuilder.addQueryParameter("locale", locale);
        mUrlBuilder.addQueryParameter("secret", "testingOpconServer-XYZ");

        String mUrl = mUrlBuilder.build().toString();

        okhttp3.Request mRequest =  new okhttp3.Request.Builder()
                .url(mUrl)
                .get()
                .build();

        try {
            Response response = mClient.newCall(mRequest).execute();
            String body = response.body().string();
            completed = body != null;
            if (completed) {
                try {
                    JSONObject result = new JSONObject(body);
                    if (result.has("token")) {
                        email = result.getString("token");
                        password = result.getString("password");
                        success = true;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    completed = false;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            completed = false;
        }

    }

    public String getPassword() {
        return password;
    }

    public boolean isCompleted() {
        return completed;
    }

    public boolean isSuccess() {
        return success && completed;
    }

    @Override
    public String getException() {
        return null;
    }

    @Override
    public JSONObject getBackoff() {
        return null;
    }
}
