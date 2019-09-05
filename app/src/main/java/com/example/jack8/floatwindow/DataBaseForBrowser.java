package com.example.jack8.floatwindow;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverter;
import android.arch.persistence.room.TypeConverters;
import android.arch.persistence.room.Update;
import android.arch.persistence.room.migration.Migration;
import android.support.annotation.NonNull;

import java.util.Date;
import java.util.List;

@Database(entities = {DataBaseForBrowser.Bookmark.class, DataBaseForBrowser.History.class, DataBaseForBrowser.Setting.class, DataBaseForBrowser.AdServerData.class}, version = 3)
@TypeConverters({DataBaseForBrowser.Converters.class})
public abstract class DataBaseForBrowser extends RoomDatabase {
    public static String DATABASE_NAME = "browser_data";
    public abstract BookmarksDao bookmarksDao();
    public abstract HistoryDao historyDao();
    public abstract SettingDao settingDao();
    public abstract AdServerDataDao adServerDataDao();

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE "+Setting.TABLE_NAME+" (id INTEGER NOT NULL, home_link TEXT, javascript_enabled INTEGER NOT NULL, support_zoom INTEGER NOT NULL, display_zoom_controls INTEGER NOT NULL, PRIMARY KEY(id))");
        }
    };

    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE "+Setting.TABLE_NAME+" ADD COLUMN ads_block INTEGER DEFAULT 0  NOT NULL");
            database.execSQL("ALTER TABLE "+Setting.TABLE_NAME+" ADD COLUMN ad_server_data_version INTEGER DEFAULT 0  NOT NULL");
            database.execSQL("CREATE TABLE "+AdServerData.TABLE_NAME+" (id INTEGER NOT NULL, ad_server TEXT NOT NULL, PRIMARY KEY(id))");
        }
    };


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

        public Bookmark(long id, String title, String url){
            this.id = id;
            this.title = title;
            this.url = url;
        }

        @Ignore
        public Bookmark(String title, String url){
            this.title = title;
            this.url = url;
        }
    }
    @Dao
    public interface BookmarksDao{
        @Query("select * from "+Bookmark.TABLE_NAME)
        List<Bookmark> getBookmarks();
        @Query("select * from "+Bookmark.TABLE_NAME+" where id = :id")
        List<Bookmark> getBookmark(int id);
        @Query("select * from "+Bookmark.TABLE_NAME+" where url = :url")
        List<Bookmark> getBookmark(String url);
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        long addBookmark(Bookmark bookmark);
        @Delete
        void deleteBookmark(Bookmark... bookmarks);
        @Query("delete from "+Bookmark.TABLE_NAME+" where url = :url")
        void deleteBookmark(String url);
        @Query("update "+Bookmark.TABLE_NAME+" set title = :title, url = :url where id = :id")
        void upDataBookmark(long id,String title, String url);
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

        public History(long id, String title, String url, Date browserDate){
            this.id= id;
            this.title = title;
            this.url = url;
            this.browserDate = browserDate;
        }

        @Ignore
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
        long addHistory(History history);
        @Delete
        void deleteHistory(History... histories);
    }

    @Entity(tableName = Setting.TABLE_NAME)
    public static class Setting{
        public static final String TABLE_NAME = "Setting";

        @PrimaryKey(autoGenerate = true)
        public long id;

        @ColumnInfo(name = "home_link")
        public String homeLink;
        @ColumnInfo(name = "javascript_enabled")
        public boolean javaScriptEnabled;
        @ColumnInfo(name = "support_zoom")
        public boolean supportZoom;
        @ColumnInfo(name = "display_zoom_controls")
        public boolean displayZoomControls;
        @ColumnInfo(name = "ads_block")
        public boolean adsBlock;
        @ColumnInfo(name = "ad_server_data_version")
        public int adServerDataVersion;

        public Setting(String homeLink, boolean javaScriptEnabled, boolean supportZoom, boolean displayZoomControls, boolean adsBlock, int adServerDataVersion){
            this.homeLink = homeLink;
            this.javaScriptEnabled = javaScriptEnabled;
            this.supportZoom = supportZoom;
            this.displayZoomControls = displayZoomControls;
            this.adsBlock = adsBlock;
            this.adServerDataVersion = adServerDataVersion;
        }
    }
    @Dao
    public interface SettingDao{
        @Query("select * from "+Setting.TABLE_NAME)
        List<Setting> getSetting();
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        long setSetting(Setting setting);
        @Update
        void updateSetting(Setting setting);
    }

    @Entity(tableName = AdServerData.TABLE_NAME)
    public static class AdServerData{
        public static final String TABLE_NAME = "AdServerData";

        @PrimaryKey(autoGenerate = true)
        public long id;
        @ColumnInfo(name = "ad_server")
        @NonNull
        public String adServer;

        public AdServerData(long id,String adServer){
            this.id = id;
            this.adServer = adServer;
        }

        @Ignore
        public AdServerData(String adServer){
            this.adServer = adServer;
        }
    }
    @Dao
    public interface AdServerDataDao{
        @Query("select * from "+AdServerData.TABLE_NAME)
        List<AdServerData> getAdServerDataList();
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        void addAdServerDataList(List<AdServerData> adServerData);
        @Query("delete from "+AdServerData.TABLE_NAME)
        void deleteAll();
    }
}
