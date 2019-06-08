package com.example.jack8.floatwindow;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverter;
import android.arch.persistence.room.TypeConverters;

import java.util.Date;
import java.util.List;

@Database(entities = {DataBaseForBrowser.Bookmark.class, DataBaseForBrowser.History.class}, version = 1)
@TypeConverters({DataBaseForBrowser.Converters.class})
public abstract class DataBaseForBrowser extends RoomDatabase {
    public static String DATABASE_NAME = "browser_data";
    public abstract BookmarksDao bookmarksDao();
    public abstract HistoryDao historyDao();

    static class Converters {
        @TypeConverter
        public static Date fromTimestamp(Long value) {
            return value == null ? null : new Date(value);
        }

        @TypeConverter
        public static Long dateToTimestamp(Date date) {
            return date == null ? null : date.getTime();
        }
    }

    @Entity(tableName = Bookmark.TABLE_NAME, indices = {@Index(value = {"url"}, unique = true)})
    public static class Bookmark{
        public static final String TABLE_NAME = "bookmarks";

        @PrimaryKey(autoGenerate = true)
        public long id;

        public String title;
        @ColumnInfo(name = "url")
        public String url;
    }
    @Dao
    public interface BookmarksDao{
        @Query("select * from "+Bookmark.TABLE_NAME)
        List<Bookmark> getBookmarks();
        @Query("select * from "+Bookmark.TABLE_NAME+" where id = :id")
        List<Bookmark> getBookmark(int id);
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        void addBookmark(Bookmark bookmark);
    }

    @Entity(tableName = History.TABLE_NAME, indices = {@Index(value = {"url"}, unique = true)})
    public static class History{
        public static final String TABLE_NAME = "History";

        @PrimaryKey(autoGenerate = true)
        public long id;

        public String title;
        @ColumnInfo(name = "url")
        public String url;
        @ColumnInfo(name = "browser_date")
        public Date browserDate;

        public History(String title, String url, Date browserDate){
            this.title = title;
            this.url = url;
            this.browserDate = browserDate;
        }
    }
    @Dao
    public interface HistoryDao{
        @Query("select * from "+History.TABLE_NAME+" order by browser_date desc")
        List<History> getAllHistory();
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        void addHistory(History history);
    }
}
