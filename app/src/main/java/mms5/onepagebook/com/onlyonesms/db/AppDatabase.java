package mms5.onepagebook.com.onlyonesms.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import mms5.onepagebook.com.onlyonesms.db.dao.CallMsgDao;
import mms5.onepagebook.com.onlyonesms.db.dao.ImageBoxDao;
import mms5.onepagebook.com.onlyonesms.db.dao.MsgDao;
import mms5.onepagebook.com.onlyonesms.db.dao.TelNumBlockDao;
import mms5.onepagebook.com.onlyonesms.db.dao.TelNumPermitDao;
import mms5.onepagebook.com.onlyonesms.db.entity.CallMsg;
import mms5.onepagebook.com.onlyonesms.db.entity.ImageBox;
import mms5.onepagebook.com.onlyonesms.db.entity.Msg;
import mms5.onepagebook.com.onlyonesms.db.entity.TelNumBlock;
import mms5.onepagebook.com.onlyonesms.db.entity.TelNumPermit;

@Database(version = 2, entities = {Msg.class, ImageBox.class, TelNumBlock.class, TelNumPermit.class, CallMsg.class}, exportSchema = false)
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
                DB_NAME).fallbackToDestructiveMigration().build();
    }

    abstract public MsgDao getMsgDao();
    abstract public ImageBoxDao getImageBoxDao();
    abstract public TelNumPermitDao getTelNumPermitDao();
    abstract public TelNumBlockDao getTelNumBlockDao();
    abstract public CallMsgDao getCallMsgDao();
}
