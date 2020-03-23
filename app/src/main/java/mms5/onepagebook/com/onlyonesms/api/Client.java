package mms5.onepagebook.com.onlyonesms.api;

import mms5.onepagebook.com.onlyonesms.api.body.CheckTaskBody;
import mms5.onepagebook.com.onlyonesms.api.body.GettingStatisticsBody;
import mms5.onepagebook.com.onlyonesms.api.body.GettingTaskBody;
import mms5.onepagebook.com.onlyonesms.api.body.ReportSendingResultBody;
import mms5.onepagebook.com.onlyonesms.api.body.SendingChangedNumberBody;
import mms5.onepagebook.com.onlyonesms.api.body.SendingStatusBody;
import mms5.onepagebook.com.onlyonesms.api.body.SendingStatusBodyEnd;
import mms5.onepagebook.com.onlyonesms.api.body.ServiceListBody;
import mms5.onepagebook.com.onlyonesms.api.body.SignInBody;
import mms5.onepagebook.com.onlyonesms.api.body.SyncContactBody;
import mms5.onepagebook.com.onlyonesms.api.response.DefaultResult;
import mms5.onepagebook.com.onlyonesms.model.ServiceList;
import mms5.onepagebook.com.onlyonesms.model.Statistics;
import mms5.onepagebook.com.onlyonesms.model.Task;
import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface Client {

    @FormUrlEncoded
    @POST("/omm/app_login.php")
    Call<DefaultResult> signIn(@FieldMap SignInBody body);

    @FormUrlEncoded
    @POST("/omm/app_login_id.php")
    Call<DefaultResult> signInWithoutPw(@FieldMap SignInBody body);

    @FormUrlEncoded
    @POST("/mms/check_task.php")
    Call<DefaultResult> checkTasks(@FieldMap CheckTaskBody body);

    @FormUrlEncoded
    @POST("/mms/get_task.php")
    Call<Task> getTasks(@FieldMap GettingTaskBody body);

    @FormUrlEncoded
    @POST("/mms/send_complete_num.php")
    Call<DefaultResult> reportSendingResult(@FieldMap ReportSendingResultBody body);

    @FormUrlEncoded
    @POST("/mms/sync_address.php")
    Call<DefaultResult> syncContacts(@FieldMap SyncContactBody body);

    @FormUrlEncoded
    @POST("/mms/receive_sms.php")
    Call<DefaultResult> sendChangedNumber(@FieldMap SendingChangedNumberBody body);

    @FormUrlEncoded
    @POST("/mms/get_statistics.php")
    Call<Statistics> getStatistics(@FieldMap GettingStatisticsBody body);

    @FormUrlEncoded
    @POST("/mms/receive_status.php")
    Call<DefaultResult> sendSendingStatus(@FieldMap SendingStatusBody body);

    @FormUrlEncoded
    @POST("/mms/service_list.php")
    Call<ServiceList> serviceList(@FieldMap ServiceListBody body);

    @FormUrlEncoded
    @POST("/mms/receive_status_end.php")
    Call<DefaultResult> sendSendingStatusEnd(@FieldMap SendingStatusBodyEnd body);
}