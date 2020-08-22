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
    private static List<BookmarkCollection> mCollections = new ArrayList<>();

    private class BookmarkCollection {
        public String mName;
        public String mLink;

        public BookmarkCollection(String name, String link) {
            mName = name;
            mLink = link;
        }
    }

    public static class Bookmark {
        public List<Integer> mCollectionIndexs = new ArrayList<Integer>();
        public String mName;
        public String mLink;

        public Bookmark(List<String> collectionLinks, String name, String link) {
            mName = name;
            mLink = link;
            mCollectionIndexs.add(0);
            for (int i = 1; i < mCollections.size(); i++) {
                for (int j = 0; i < collectionLinks.size(); i++) {
                    if (mCollections.get(i).mLink.equals(collectionLinks.get(j))) {
                        mCollectionIndexs.add(i);
                    }
                }
            }
        }

        public Bookmark(JSONArray collectionLinks, String name, String link) throws JSONException {
            mName = name;
            mLink = link;
            mCollectionIndexs.add(0);
            List<String> collectionStrings = new ArrayList<>();

            for (int i = 1; i < collectionLinks.length(); i++) {
                collectionStrings.add(collectionLinks.getString(i));
            }

            for (int i = 1; i < mCollections.size(); i++) {
                for (int j = 0; i < collectionStrings.size(); i++) {
                    if (mCollections.get(i).mLink.equals(collectionStrings.get(j))) {
                        mCollectionIndexs.add(i);
                    }
                }
            }
        }

        public int collectionIndexFromLink(String collectionLink) {
            for (int i = 0; i < mCollectionIndexs.size(); i++) {
                if (mCollections.get(mCollectionIndexs.get(i)).mLink.equals(collectionLink)) {
                    return i;
                }
            }
            return -1;
        }

        public boolean isInCollection(String collectionLink) {
            return collectionIndexFromLink(collectionLink) >= 0;
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
        if (file.length() == 0) {
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
                    jsonBookmark.getJSONArray("collection"),
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
            // TODO collection indexs needs to be an array
            JSONArray jsonCollections = new JSONArray();
            for (int j = 0; j < mBookmarks.get(i).mCollectionIndexs.size(); i++) {
                jsonCollections.put(mBookmarks.get(i).mCollectionIndexs.get(j));
            }
            jsonBookmark.put("collection", jsonCollections);
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

    public boolean isBookmarked(String link) {
        for (int i = 0; i < bookmarksLength(); i++) {
            if (mBookmarks.get(i).mLink.equals(link))
                return true;
        }
        return false;
    }

    public void addBookmark(List<String> collections, String name, String link) {
        Bookmark bookmark = new Bookmark(collections, name, link);
        mBookmarks.add(bookmark);
    }
    public void addBookmark(String name, String link) {
        Bookmark bookmark = new Bookmark(new ArrayList<String>(), name, link);
        mBookmarks.add(bookmark);
    }

    public void removeBookmark(String collection, String link) {
        int index = findBookmarkIndexByLink(link);
        if (index != -1) {
            if (mCollections.size() == 1 || collection.equals("_all_")) {
                mBookmarks.remove(index);
            } else {
                int i = mBookmarks.get(index).collectionIndexFromLink(collection);
                if (i >= 0) {
                    mBookmarks.get(index).mCollectionIndexs.remove(i);
                }
            }
        }
    }

    public String getCollectionName(int index) {
        return mCollections.get(index).mName;
    }
    public String getCollectionLink(int index) {
        return mCollections.get(index).mLink;
    }

    public List<String> getBookmarkCollection(int index) {
        List<String> collectionLinks = new ArrayList<>();
        for (int i = 0; i < mBookmarks.get(index).mCollectionIndexs.size(); i++) {
            collectionLinks.add(
                    mCollections.get(mBookmarks.get(index).mCollectionIndexs.get(i)).mLink);
        }
        return collectionLinks;
    }
    public String getBookmarkName(int index) {
        return mBookmarks.get(index).mName;
    }
    public String getBookmarkLink(int index) {
        return mBookmarks.get(index).mLink;
    }

    public int findBookmarkIndexByLink(String link) {
        for (int i = 0; i < bookmarksLength(); i++) {
            if (getBookmarkLink(i).equals(link)) {
                return i;
            }
        }
        return -1;
    }
}
