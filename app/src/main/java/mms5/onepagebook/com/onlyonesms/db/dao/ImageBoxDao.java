package mms5.onepagebook.com.onlyonesms.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import mms5.onepagebook.com.onlyonesms.db.entity.ImageBox;

@Dao
public interface ImageBoxDao {
    @Query("SELECT * FROM imagebox")
    List<ImageBox> getAll();

    @Insert
    void insert(ImageBox iBox);

    @Update
    void update(ImageBox iBox);

    @Delete
    void delete(ImageBox iBox);
}
