package mms5.onepagebook.com.onlyonesms.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import mms5.onepagebook.com.onlyonesms.db.entity.Msg;

@Dao
public interface MsgDao {
    @Query("SELECT * FROM msg")
    List<Msg> getAll();

    @Query("SELECT * FROM msg WHERE mtype = :mt AND useyn = 'Y'")
    Msg findByTypeOnUse(String mt);

    @Query("SELECT * FROM msg WHERE mtype = :mt")
    List<Msg> findByType(String mt);

    @Insert
    void insert(Msg msg);

    @Update
    void update(Msg msg);

    @Delete
    void delete(Msg msg);

    @Query("UPDATE msg SET useyn = 'N' WHERE mtype = :mt AND useyn = 'Y'")
    int updateUseYnYtoN(String mt);

    @Query("UPDATE msg SET useyn = :uYn WHERE lastupdate = :updateTime")
    int updateUseYnByUpdateTime(long updateTime, String uYn);
}
