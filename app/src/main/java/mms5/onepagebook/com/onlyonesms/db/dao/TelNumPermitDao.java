package mms5.onepagebook.com.onlyonesms.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import mms5.onepagebook.com.onlyonesms.db.entity.TelNumPermit;

@Dao
public interface TelNumPermitDao {
    @Query("SELECT * FROM telnumpermit")
    List<TelNumPermit> getAll();

    @Insert
    void insert(TelNumPermit telNumPermit);

    @Update
    void update(TelNumPermit telNumPermit);

    @Delete
    void delete(TelNumPermit telNumPermit);
}
