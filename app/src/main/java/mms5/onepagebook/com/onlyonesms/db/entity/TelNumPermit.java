package mms5.onepagebook.com.onlyonesms.db.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "telnumpermit")
public class TelNumPermit {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "tid")
    public long tid;

    @ColumnInfo(name = "name")
    public String name; //이름

    @ColumnInfo(name = "num")
    public String num; //연락처
}
