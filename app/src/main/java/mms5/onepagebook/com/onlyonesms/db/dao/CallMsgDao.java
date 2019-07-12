package mms5.onepagebook.com.onlyonesms.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import mms5.onepagebook.com.onlyonesms.db.entity.CallMsg;

/**
 * Created by jeonghopark on 2019-07-11.
 */
@Dao
public interface CallMsgDao {
    @Query("SELECT * FROM callmsg")
    List<CallMsg> getAll();

    @Insert
    void insert(CallMsg callMsg);

    @Update
    void update(CallMsg callMsg);

    @Delete
    void delete(CallMsg callMsg);
}
