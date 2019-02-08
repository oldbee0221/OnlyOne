package mms5.onepagebook.com.onlyonesms.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import mms5.onepagebook.com.onlyonesms.db.dao.MsgDao;
import mms5.onepagebook.com.onlyonesms.db.entity.Msg;

@Database(version = 1, entities = {Msg.class})
public abstract class AppDatabase extends RoomDatabase {

    private static final String DB_NAME = "olnyonessm.db";
    private static volatile AppDatabase instance;

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = create(context);
        }
        return instance;
    }

    private static AppDatabase create(final Context context) {
        return Room.databaseBuilder(
                context,
                AppDatabase.class,
                DB_NAME).build();
    }

    abstract public MsgDao getMsgDao();
}
