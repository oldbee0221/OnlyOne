package mms5.onepagebook.com.onlyonesms.manager;

import android.content.Context;
import android.support.annotation.NonNull;

import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;

import java.io.IOException;

import mms5.onepagebook.com.onlyonesms.MainActivity;
import mms5.onepagebook.com.onlyonesms.util.Utils;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RetrofitManager {
  public static final String BASE_URL = "https://www.obmms.net/";
  public static final String URL_SIGN_UP = "https://www.obmms.net/join.php";

  private static Retrofit retrofit = null;
  private static OkHttpClient client = null;

  private static PersistentCookieJar cookieJar = null;

  public static Retrofit retrofit(Context context) {
    if (retrofit == null) {
      OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
      httpClient.cookieJar(cookieJar(context));
      httpClient.addInterceptor(new NetworkCheckInterceptor(context));
      String bUrl = PreferenceManager.getInstance(context).getBaseUrl();

      if (MainActivity.HAS_TO_SHOW_LOGS) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        httpClient.addInterceptor(logging);
      }

      client = httpClient.build();
      retrofit = new Retrofit.Builder()
        .baseUrl(bUrl)
        .client(client)
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(GsonConverterFactory.create(GsonManager.getGson()))
        .build();
    }
    return retrofit;
  }

  public static void cleanRetrofit() {
    retrofit = null;
  }

  private static PersistentCookieJar cookieJar(Context context) {
    if (cookieJar == null) {
      cookieJar = new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(context));
    }
    return cookieJar;
  }

  public static void clearCookie() {
    if (cookieJar != null) {
      cookieJar.clearSession();
      cookieJar.clear();
    }
  }

  public static class NetworkCheckInterceptor implements Interceptor {
    private Context context;

    public NetworkCheckInterceptor(Context context) {
      this.context = context;
    }

    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
      if (Utils.isNetworkStateFine(context)) {
        return chain.proceed(chain.request());
      }
      else {
        throw new NoConnectivityException();
      }
    }
  }

  public static class NoConnectivityException extends IOException {
    @Override
    public String getMessage() {
      return "No network available, please check your WiFi or Data connection";
    }
  }
}