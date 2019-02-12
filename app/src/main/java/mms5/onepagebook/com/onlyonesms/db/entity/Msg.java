package mms5.onepagebook.com.onlyonesms.db.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "msg")
public class Msg {
    @PrimaryKey
    @ColumnInfo(name = "lastupdate")
    public long lastUpdateTime; //최종 수정 시각 (밀리세컨드)

    @ColumnInfo(name = "mtype")
    public String msgType; // 수신, 부재중, 발신

    @ColumnInfo(name = "dayofweek")
    public String dayOfWeek; // 월화수목금토일

    @ColumnInfo(name = "stime")
    public int startTime; // 시작시간 (네자리수 오후 1시 34분 => 1334)

    @ColumnInfo(name = "etime")
    public int endTime; // 종료시간 (네자리수 오후 1시 34분 => 1334)

    @ColumnInfo(name = "stype")
    public String sendType; //발송방법 (메시지 바로 전송, 확인 후 전송)

    @ColumnInfo(name = "soption")
    public String sendOption; //같은 번호 발송 옵션 (중복발송 허용, 하루 1회, 일주일 1회, 한달 1회)

    @ColumnInfo(name = "imgpath")
    public String imgPath; //이미지 경로

    @ColumnInfo(name = "ad")
    public String adYn; //광고 여부

    @ColumnInfo(name = "message1")
    public String message1;

    @ColumnInfo(name = "message2")
    public String message2;

    @ColumnInfo(name = "allday")
    public String allDayYn; //하루종일 여부

    @ColumnInfo(name = "sendOnAbs")
    public String sendOnAbsYn; //부재중 발신 여부

    @ColumnInfo(name = "useyn")
    public String useYn; //적용 여부
}
