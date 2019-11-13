package mms5.onepagebook.com.onlyonesms.api.body;

import java.util.HashMap;

/**
 * Created by jeonghopark on 2019-11-11.
 */
public class SendingStatusBodyEnd extends HashMap<String, String> {

    public SendingStatusBodyEnd(String idx,
                                String sendNumber,
                                String phoneNumber,
                                String isSent,
                                String endTime) {
        put("idx", idx);
        put("send_num", sendNumber);
        put("recv_num", phoneNumber);
        put("status", isSent);
        put("end_time", endTime);
    }
}
