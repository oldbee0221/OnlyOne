package mms5.onepagebook.com.onlyonesms.db.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "imagebox")
public class ImageBox {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "iid")
    public long iid;

    @ColumnInfo(name = "imgpath")
    public String imgPath; //이미지 경로
}
