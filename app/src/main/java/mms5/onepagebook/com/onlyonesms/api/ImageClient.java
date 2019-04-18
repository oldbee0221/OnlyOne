package mms5.onepagebook.com.onlyonesms.api;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface ImageClient {
    @GET
    Call<ResponseBody> downloadImage(@Url String url);
}
