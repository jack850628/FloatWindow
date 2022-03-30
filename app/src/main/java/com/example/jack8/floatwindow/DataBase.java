package com.example.jack8.floatwindow;

import android.content.Context;

import androidx.room.ColumnInfo;
import androidx.room.Dao;
import androidx.room.Database;
import androidx.room.Delete;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.PrimaryKey;
import androidx.room.Query;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.List;

@Database(entities = {DataBase.WorkingWindow.class}, version = 1)
public abstract class DataBase extends RoomDatabase {
    public static String DATABASE_NAME = "float_window_data";
    public abstract WorkingWindowDao workingWindowDao();

    @Entity(tableName = WorkingWindow.TABLE_NAME)
    public static class WorkingWindow{
        public static final String TABLE_NAME = "WorkingWindow";

        @PrimaryKey
        public long windowId;

        @ColumnInfo(name = "uri")
        public String uri;

        public WorkingWindow(long windowId, String uri){
            this.windowId = windowId;
            this.uri = uri;
        }
    }

    @Dao
    public interface WorkingWindowDao{
        @Query("select * from " + WorkingWindow.TABLE_NAME)
        List<WorkingWindow> getAllWorkingWindow();

        @Query("select * from " + WorkingWindow.TABLE_NAME + " where windowId = :windowId")
        List<WorkingWindow> getWorkingWindow(int windowId);

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        long addWorkingWindow(WorkingWindow workingWindow);

        @Query("delete from " + WorkingWindow.TABLE_NAME + " where windowId = :windowId")
        void deleteWorkingWindow(int windowId);

        @Query("delete from " + WorkingWindow.TABLE_NAME)
        void deleteAllWorkingWindow();
    }

    private static DataBase dataBase;

    public static DataBase getInstance(Context context){
        if(dataBase == null){
            dataBase = Room.databaseBuilder(context, DataBase.class, DataBase.DATABASE_NAME)
                .build();
        }
        return dataBase;
    }
}
