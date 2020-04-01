package com.alading.dream.cache;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.alading.libcommon.global.AppGlobals;

@Database(entities = {Cache.class},version = 1, exportSchema = true)
public abstract class CacheDatabase extends RoomDatabase {
    static CacheDatabase database;
    static {
        //创建一个内存数据库
        //但是这种数据库的数据只存在于内存中,也就是进出被杀之后,数据随之丢失
//        Room.inMemoryDatabaseBuilder()
          database = Room.databaseBuilder(AppGlobals.getApplication(), CacheDatabase.class, "dream_cache")

//                .allowMainThreadQueries()

                //数据库创建和打开后的回调
//                .addCallback()

//        .setQueryExecutor()

//        .openHelperFactory()

                // room的日志模式
//        .setJournalMode()

                //数据库升级异常之后的回滚
//        .fallbackToDestructiveMigration()

                //数据库升级异常后根据指定版本进行回滚
//        .fallbackToDestructiveMigrationFrom()

//                .addMigrations(CacheDatabase.sMigration)

                .build();

    }

    public static CacheDatabase get(){
        return database;
    }

    public abstract CacheDao getCache();

//    static Migration sMigration = new Migration(1,3) {
//        @Override
//        public void migrate(@NonNull SupportSQLiteDatabase database) {
//            database.execSQL("alter table teacher rename to student");
//            database.execSQL("alter table teacher add colume teacher_age INTEGER NOT NULL default 0");
//
//        }
//    };
}
