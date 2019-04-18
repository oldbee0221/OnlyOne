package mms5.onepagebook.com.onlyonesms.api;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;

import mms5.onepagebook.com.onlyonesms.BuildConfig;
import mms5.onepagebook.com.onlyonesms.manager.GsonManager;
import mms5.onepagebook.com.onlyonesms.manager.RetrofitManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class ApiCallback<T> implements Callback<T> {
    public static final int ERROR_NETWORK = -1;
    public static final int ERROR_SERVER = -2;
    public static final int ERROR_AUTHORIZATION = -3;
    public static final int ERROR_SERVER_OFF = -4;
    public static final int ERROR_ELSE = -10;

    public String errorBody;

    public ApiCallback() {
    }

    public abstract void onSuccess(T response);

    public abstract void onFail(int error, String msg);

    @Override
    public void onResponse(@NonNull Call<T> call, @NonNull Response<T> response) {
        if (response.isSuccessful()) {
            if (BuildConfig.DEBUG) {
                try {
                    Log.i("RESPONSE", "API: " + call.request().url().toString());
                    Log.i("RESPONSE", "RESPONSE: " + GsonManager.getGson().toJson(response.body()));
                } catch (Exception e) {
                }
            }
            onSuccess(response.body());
        } else {
            try {
                if (response.errorBody() != null) {
                    if (!TextUtils.isEmpty(response.errorBody().string())) {
                        errorBody = response.errorBody().toString();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            switch (response.code()) {
                case 400:
                    onFail(ERROR_AUTHORIZATION, "잘못된 접근입니다, STATUS_CODE: " + 400);
                    break;

                case 401:
                    onFail(ERROR_AUTHORIZATION, "유효한 유저가 아닙니다, STATUS_CODE: " + 401);
                    break;

                case 500:
                    onFail(ERROR_AUTHORIZATION, "서버에 문제가 있습니다, STATUS_CODE: " + 500);
                    break;

                case 501:
                    onFail(ERROR_AUTHORIZATION, "서버에 문제가 있습니다, STATUS_CODE: " + 501);
                    break;

                case 502:
                    onFail(ERROR_AUTHORIZATION, "서버에 문제가 있습니다, STATUS_CODE: " + 502);
                    break;

                case 503:
                    onFail(ERROR_AUTHORIZATION, "서버에 문제가 있습니다, STATUS_CODE: " + 503);
                    break;

                default:
                    onFail(ERROR_ELSE, "네트워크에 문제가 있습니다");
                    break;

            }
        }
    }

    @Override
    public void onFailure(@NonNull Call<T> call, @NonNull Throwable t) {
        t.printStackTrace();
        if (t instanceof RetrofitManager.NoConnectivityException) {
            Log.e("ApiCallback", "NetworkError!");
            onFail(ERROR_NETWORK, "네트워크에 문제가 있습니다");
        } else {
            Log.e("ApiCallback", "t.class: " + t.getClass().getSimpleName());
            onFail(ERROR_ELSE, Log.getStackTraceString(t));
        }
    }

    public class Event {
        public static final String ERROR_RESTART = "error_restart";
        public static final String ERROR_RESTART_WITH_MSG = "restart_with_msg";
        public static final String ERROR_LOGIN = "error_login";

        public String error;
        public String message;

        public Event(String error) {
            this.error = error;
        }
    }
}