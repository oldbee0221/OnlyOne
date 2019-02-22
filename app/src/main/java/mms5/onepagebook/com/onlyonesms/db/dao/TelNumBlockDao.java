package mms5.onepagebook.com.onlyonesms.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import mms5.onepagebook.com.onlyonesms.db.entity.TelNumBlock;

@Dao
public interface TelNumBlockDao {
    @Query("SELECT * FROM telnumblock")
    List<TelNumBlock> getAll();

    @Insert
    void insert(TelNumBlock telNumBlock);

    @Update
    void update(TelNumBlock telNumBlock);

    @Delete
    void delete(TelNumBlock telNumBlock);
}
