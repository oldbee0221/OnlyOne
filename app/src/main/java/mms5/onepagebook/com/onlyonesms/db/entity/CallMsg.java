package mms5.onepagebook.com.onlyonesms.db.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by jeonghopark on 2019-07-11.
 */
@Entity(tableName = "callmsg")
public class CallMsg {
    @PrimaryKey
    @ColumnInfo(name = "regdate")
    public long regdate; //입력 시각 (밀리세컨드)

    @ColumnInfo(name = "category")
    public String category;

    @ColumnInfo(name = "title")
    public String title;

    @ColumnInfo(name = "imgpath")
    public String imgpath;

    @ColumnInfo(name = "contents")
    public String contents;
}
