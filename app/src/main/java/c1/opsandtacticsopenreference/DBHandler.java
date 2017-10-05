package c1.opsandtacticsopenreference;

/**
 * Created by c1user on 10/5/17.
 */

import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.List;
import java.util.ArrayList;

public class DBHandler extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "bookmarks_db";
    // Bookmarks table names
    private static final String TABLE_BOOKMARKS = "bookmarks";
    private static final String TABLE_COLLECTIONS = "collections";
    private static final String TABLE_BOOK_COLLECT = "book_collect";
    // Common Table Columns names
    private static final String KEY_ID = "id";
    // Collection Table Column names
    private static final String KEY_COLLECTION = "collections";
    // Bookmark Table Column names
    private static final String KEY_TEXT = "text";
    private static final String KEY_LINK = "link";
    private static final String KEY_TYPE = "type";
    // Bookmark Collection Table Column names
    private static final String KEY_BOOKMARK_ID = "";
    private static final String KEY_COLLECTION_ID = "";
    public DBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_BOOKMARKS_TABLE = "CREATE TABLE " + TABLE_BOOKMARKS + "("
            + KEY_ID + " INTEGER PRIMARY KEY," + KEY_TEXT + " TEXT,"
            + KEY_LINK + " TEXT," + KEY_TYPE + " TEXT" + ")";
        String CREATE_COLLECTIONS_TABLE = "CREATE TABLE " + TABLE_COLLECTIONS + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_COLLECTION + " TEXT" + ")";
        String CREATE_BOOK_COLLECT_TABLE = "CREATE TABLE " + TABLE_BOOK_COLLECT + "("
            + KEY_ID + " INTEGER PRIMARY KEY," + KEY_BOOKMARK_ID + " INTEGER,"
            + KEY_COLLECTION_ID + " INTEGER" + ")";
        db.execSQL(CREATE_BOOKMARKS_TABLE);
        db.execSQL(CREATE_COLLECTIONS_TABLE);
        db.execSQL(CREATE_BOOK_COLLECT_TABLE);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKMARKS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COLLECTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOK_COLLECT);
        // Creating tables again
        onCreate(db);
    }
    // Adding new bookmark
    public long addBookmark(Bookmark bookmark, int[] category_ids) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_TEXT, bookmark.getText()); // Bookmark Text
        values.put(KEY_LINK, bookmark.getLink()); // Bookmark Link
        values.put(KEY_TYPE, bookmark.getType()); // Bookmark Type
        // Inserting Row
        long bookmark_id = db.insert(TABLE_BOOKMARKS, null, values);

        for (int category_id : category_ids){
            //createBookCollect(bookmark_id, category_id);
        }

        return bookmark_id;
    }
    // Getting one bookmark
    public Bookmark getBookmark(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT  * FROM " + TABLE_BOOKMARKS + " WHERE "
                + KEY_ID + " = " + id;
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor != null)
            cursor.moveToFirst();

        Bookmark bookmark = new Bookmark();
        bookmark.setId(cursor.getInt(cursor.getColumnIndex(KEY_ID)));
        bookmark.setText(cursor.getString(cursor.getColumnIndex(KEY_TEXT)));
        bookmark.setLink(cursor.getString(cursor.getColumnIndex(KEY_LINK)));
        bookmark.setType(cursor.getString(cursor.getColumnIndex(KEY_TYPE)));

        return bookmark;
    }
    // Get all bookmarks
    public List<Bookmark> getAllBookmarks() {
        List<Bookmark> bookmarkList = new ArrayList<Bookmark>();
        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_BOOKMARKS;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Bookmark bookmark = new Bookmark();
                bookmark.setId(cursor.getInt(cursor.getColumnIndex(KEY_ID)));
                bookmark.setText(cursor.getString(cursor.getColumnIndex(KEY_TEXT)));
                bookmark.setLink(cursor.getString(cursor.getColumnIndex(KEY_LINK)));
                bookmark.setType(cursor.getString(cursor.getColumnIndex(KEY_TYPE)));
                // Adding contact to list
                bookmarkList.add(bookmark);
            } while (cursor.moveToNext());
        }
        // return contact list
        return bookmarkList;
    }
    // Get all bookmarks in a collection
    public List<Bookmark> getAllBookmarksByCollection(String collection_name) {
        List<Bookmark> bookmarks = new ArrayList<Bookmark>();
        String selectQuery = "SELECT * FROM " + TABLE_BOOKMARKS + "td, "
                + TABLE_COLLECTIONS + "tg, " + TABLE_BOOK_COLLECT + "tt WHERE tg."
                + KEY_COLLECTION + " = '" + collection_name + "'" + "AND tg." + KEY_ID
                + " = " + "tt." + KEY_COLLECTION_ID +" AND td." + KEY_ID + " = "
                + "tt." + KEY_BOOKMARK_ID;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Bookmark td = new Bookmark();
                td.setId(cursor.getInt((cursor.getColumnIndex(KEY_ID))));
                td.setText(cursor.getString(cursor.getColumnIndex(KEY_TEXT)));
                td.setLink(cursor.getString(cursor.getColumnIndex(KEY_LINK)));
                td.setType(cursor.getString(cursor.getColumnIndex(KEY_TYPE)));

                // adding to bookmark list
                bookmarks.add(td);
            } while (cursor.moveToNext());
        }

        return bookmarks;
    }
    // Updating a bookmark
    public int updateBookmark(Bookmark bookmark) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_TEXT, bookmark.getText()); // Bookmark Text
        values.put(KEY_LINK, bookmark.getLink()); // Bookmark Link
        values.put(KEY_TYPE, bookmark.getType()); // Bookmark Type
        // updating row
        return db.update(TABLE_BOOKMARKS, values, KEY_ID + " = ?",
                new String[]{String.valueOf(bookmark.getId())});
    }
    // Deleting a bookmark
    public void deleteBookmark(Bookmark bookmark) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_BOOKMARKS, KEY_ID + " = ?",
                new String[] { String.valueOf(bookmark.getId()) });
        db.close();
    }
}