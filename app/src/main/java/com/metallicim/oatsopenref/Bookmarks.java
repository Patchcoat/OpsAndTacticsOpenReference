package com.metallicim.oatsopenref;

import android.content.Context;
import android.os.Build;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.RequiresApi;

public class Bookmarks {
    private static Bookmarks INSTANCE = null;

    private String filename = "bookmarks.json";
    private List<Bookmark> mBookmarks = new ArrayList<>();
    private List<BookmarkCollection> mCollections = new ArrayList<>();

    private class BookmarkCollection {
        public String mName;
        public String mLink;

        public BookmarkCollection(String name, String link) {
            mName = name;
            mLink = link;
        }
    }

    private class Bookmark {
        public int mCollectionIndex;
        public String mName;
        public String mLink;

        public Bookmark(String collectionLink, String name, String link) {
            mName = name;
            mLink = link;
            for (int i = 0; i < mCollections.size(); i++) {
                if (mCollections.get(i).mLink.equals(collectionLink)) {
                    mCollectionIndex = i;
                    return;
                }
            }
            mCollectionIndex = 0;
        }
    }

    private Bookmarks() {
        if (mCollections.size() == 0) {
            mCollections.add(0, new BookmarkCollection("All Bookmarks", "_all_"));
        } else {
            mCollections.set(0, new BookmarkCollection("All Bookmarks", "_all_"));
        }
    }

    public static Bookmarks getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Bookmarks();
        }
        return INSTANCE;
    }

    public void readFile(Context context) throws IOException, JSONException {
        // Load the file
        File file = new File(context.getFilesDir(), filename);
        // if the file doesn't exist, create it
        if (!file.exists()) {
            boolean success = file.createNewFile();
            return;
        }
        // load the json
        String json;
        InputStream is = new FileInputStream(file);
        byte[] buffer = new byte[is.available()];
        is.read(buffer);
        is.close();
        json = new String(buffer, "UTF-8");
        JSONArray bookmarks = new JSONArray(json);

        // fill the bookmarks list with elements from JSON
        for (int i = 0; i < bookmarks.length(); i++) {
            JSONObject jsonBookmark = bookmarks.getJSONObject(i);
            Bookmark bookmark = new Bookmark(
                    jsonBookmark.getString("collection"),
                    jsonBookmark.getString("name"),
                    jsonBookmark.getString("link"));
            mBookmarks.add(bookmark);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void updateFile(Context context) throws IOException, JSONException {
        JSONArray jsonBookmarkArray = new JSONArray();
        for (int i = 0; i < mBookmarks.size(); i++) {
            JSONObject jsonBookmark = new JSONObject();
            jsonBookmark.put("collection", mBookmarks.get(i).mCollectionIndex);
            jsonBookmark.put("name", mBookmarks.get(i).mName);
            jsonBookmark.put("link", mBookmarks.get(i).mLink);
            jsonBookmarkArray.put(jsonBookmark);
        }
        String bookmarksString = jsonBookmarkArray.toString();

        try (FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE)) {
            fos.write(bookmarksString.getBytes("UTF-8"));
        }
    }

    public int bookmarksLength() {
        return mBookmarks.size();
    }

    public int collectionsLength() {
        return mCollections.size();
    }

    public void addBookmark(String collection, String name, String link) {
        Bookmark bookmark = new Bookmark(collection, name, link);
        mBookmarks.add(bookmark);
    }

    public String getCollectionName(int index) {
        return mCollections.get(index).mName;
    }
    public String getCollectionLink(int index) {
        return mCollections.get(index).mLink;
    }

    public String getBookmarkCollection(int index) {
        return mCollections.get(mBookmarks.get(index).mCollectionIndex).mLink;
    }
    public String getBookmarkName(int index) {
        return mBookmarks.get(index).mName;
    }
    public String getBookmarkLink(int index) {
        return mBookmarks.get(index).mLink;
    }
}
