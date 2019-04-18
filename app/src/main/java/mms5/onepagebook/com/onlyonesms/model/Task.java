package mms5.onepagebook.com.onlyonesms.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import java.io.IOException;
import java.util.ArrayList;

import mms5.onepagebook.com.onlyonesms.api.ImageClient;
import mms5.onepagebook.com.onlyonesms.manager.RetrofitManager;
import okhttp3.ResponseBody;

public class Task {
    public static final String TYPE_INDIVIDUAL = "0";
    private static final int DEFAULT_CLOSE_TIME = 21;
    private static final String DEFAULT_TITLE = "제목없음";
    private static final String DEFAULT_BODY = "내용없음";
    private static final String REP = "{|REP|}";

    public String reqid; // 문자의 ID (고유해야함)
    public String type; // 0:패키지발송, 1:개별발송 (데이터가 없을경우 기본은 패키지 발송)
    public String delay; // 발송딜레이
    public String delay2; // 값이 있을경우 delay와 delay2 사이의 값을 랜덤하게 적용하여 발송함. 반드시 delay2값이 delay보다 커야함.
    public String close; // 문자 발송 종료 시간 없을경우 기본 종료시간 21시.
    public String title; // 문자 제목 (없을경우 아래 문자 본문중 최초 8자를 제목으로 보낸다.)
    public String txt; // 문자 내용 ( {|REP|}을 치환자로 고정하며 유저가 똑같은 내용을 보낼경우 특정 문자를 특수문자로 변경한다.)
    public String jpg; // JPEG 이미지 URL (반드시 jpg로 해주세요)
    public String jpg1;
    public String jpg2;
    public ArrayList<ReceiverInfo> pnum;
    private String idx;

    public long calculateDelayInMilli(int num) {
        if (TYPE_INDIVIDUAL.equals(type)) {
            return 0;
        } else {
            int delayTime = !TextUtils.isEmpty(delay) && TextUtils.isDigitsOnly(delay) ? Integer.parseInt(delay) : 0;

            if (!TextUtils.isEmpty(delay2) && TextUtils.isDigitsOnly(delay2)) {
                int delay2Time = Integer.parseInt(delay2);
                delayTime = Math.abs((int) (Math.random() * (delay2Time - delayTime)) + delayTime);
            }
            return num % 20 == 0 ? ((long) (delayTime * 1000)) : 100L;
        }
    }

    public int closeTime() {
        if (!TextUtils.isEmpty(close) && TextUtils.isDigitsOnly(close)) {
            return Integer.parseInt(close);
        } else {
            return DEFAULT_CLOSE_TIME;
        }
    }

    public String formattedTitle(String repWord) {
        String formattedTitle = title;
        if (TextUtils.isEmpty(formattedTitle)) {
            formattedTitle = (!TextUtils.isEmpty(txt) && txt.length() > 8) ? txt.substring(0, 8) : txt;
        }

        if (formattedTitle.isEmpty() || DEFAULT_BODY.equals(txt)) {
            formattedTitle = DEFAULT_TITLE;
        }

        formattedTitle = formattedTitle.replace(REP, repWord);

        return formattedTitle;
    }

    public String formattedBody(String repWord, String bnc) {
        String formattedBody = txt;

        if (TextUtils.isEmpty(formattedBody)) {
            formattedBody = DEFAULT_BODY;
        }

        formattedBody = formattedBody.replace(REP, repWord);
        formattedBody += "\n";
        formattedBody += bnc;

        return formattedBody;
    }

    public ArrayList<ReceiverInfo> getReceiverInfoList() {
        return pnum != null ? pnum : new ArrayList<ReceiverInfo>();
    }

    public String getIdx() {
        return idx;
    }

    public void setIdx(String idx) {
        this.idx = idx;
    }

    public Bitmap[] imageBitmaps(Context context) {
        ArrayList<Bitmap> list = new ArrayList<>();
        if (!TextUtils.isEmpty(jpg)) {
            Bitmap image = downloadImage(context, jpg);
            if (image != null) {
                list.add(image);
            }
        }

        if (!TextUtils.isEmpty(jpg1)) {
            Bitmap image = downloadImage(context, jpg1);
            if (image != null) {
                list.add(image);
            }
        }

        if (!TextUtils.isEmpty(jpg2)) {
            Bitmap image = downloadImage(context, jpg2);
            if (image != null) {
                list.add(image);
            }
        }

        return list.toArray(new Bitmap[0]);
    }

    private Bitmap downloadImage(Context context, String url) {
        try {
            ResponseBody body = RetrofitManager.retrofit(context)
                    .create(ImageClient.class)
                    .downloadImage(url)
                    .execute()
                    .body();

            if (body != null) {
                byte[] bytes = body.bytes();
                return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}