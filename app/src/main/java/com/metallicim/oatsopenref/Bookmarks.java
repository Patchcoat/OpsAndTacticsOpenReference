package com.metallicim.oatsopenref;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.annotation.RequiresApi;
import androidx.collection.ArraySet;

public class Bookmarks {
    private static Bookmarks INSTANCE = null;

    private String filename = "bookmarks.json";
    private List<Bookmark> mBookmarks = new ArrayList<>();
    private static List<BookmarkCollection> mCollections = new ArrayList<>();

    private static class BookmarkCollection {
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
        public PageType mPageType;

        public Bookmark(List<String> collectionLinks, String name, String link, PageType type) {
            mName = name;
            mLink = link;
            mPageType = type;
            mCollectionIndexs.add(0);
            for (int i = 1; i < mCollections.size(); i++) {
                for (int j = 0; j < collectionLinks.size(); j++) {
                    if (mCollections.get(i).mLink.equals(collectionLinks.get(j))) {
                        mCollectionIndexs.add(i);
                    }
                }
            }
        }

        public Bookmark(JSONArray collectionLinks, String name, String link, PageType type) throws JSONException {
            mName = name;
            mLink = link;
            mPageType = type;
            mCollectionIndexs.add(0);
            List<String> collectionStrings = new ArrayList<>();

            for (int i = 1; i < collectionLinks.length(); i++) {
                collectionStrings.add(collectionLinks.getString(i));
            }

            for (int i = 1; i < mCollections.size(); i++) {
                for (int j = 0; j < collectionStrings.size(); j++) {
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
    }

    private Bookmarks() {
        if (mCollections.size() == 0) {
            mCollections.add(0, new BookmarkCollection("All Bookmarks", "_all_"));
        }
    }

    public static Bookmarks getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Bookmarks();
        }
        return INSTANCE;
    }

    public PageType stringToPageType(String str) {
        switch(str) {
            case "feat":
                return PageType.feat;
            case "xml":
                return PageType.XML;
            default:
                return PageType.error;
        }
    }
    public String pageTypeToString(PageType type) {
        switch (type) {
            case feat:
                return "feat";
            case XML:
                return "xml";
            default:
                return "error";
        }
    }

    public void readFile(Context context) throws IOException, JSONException {
        // TODO read in collections as well as bookmarks
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
        JSONObject bookmarksWithCollections = new JSONObject(json);
        JSONArray collections = bookmarksWithCollections.getJSONArray("collections");
        JSONArray bookmarks = bookmarksWithCollections.getJSONArray("bookmarks");

        // fill the collections list with elements from JSON
        for (int i = 0; i < collections.length(); i++) {
            JSONObject jsonCollection = collections.getJSONObject(i);
            BookmarkCollection collection = new BookmarkCollection(
                    jsonCollection.getString("name"),
                    jsonCollection.getString("link"));
            mCollections.add(collection);
        }

        // fill the bookmarks list with elements from JSON
        for (int i = 0; i < bookmarks.length(); i++) {
            JSONObject jsonBookmark = bookmarks.getJSONObject(i);
            PageType pageType = stringToPageType(jsonBookmark.getString("type"));
            Bookmark bookmark = new Bookmark(
                    jsonBookmark.getJSONArray("collection"),
                    jsonBookmark.getString("name"),
                    jsonBookmark.getString("link"),
                    pageType);
            mBookmarks.add(bookmark);
        }
    }

    public void updateFile(Context context) {
        // TODO write to collections as well as bookmarks
        String jsonString = "";
        try {
            // Collections Array
            JSONArray jsonCollectionArray = new JSONArray();
            // starts at 1 so it doesn't save the _all_ collection
            for (int i = 1; i < mCollections.size(); i++) {
                JSONObject jsonCollection = new JSONObject();
                jsonCollection.put("name", mCollections.get(i).mName);
                jsonCollection.put("link", mCollections.get(i).mLink);
                jsonCollectionArray.put(jsonCollection);
            }
            JSONObject bookmarksWithCollections = new JSONObject();
            // Bookmarks Array
            JSONArray jsonBookmarkArray = new JSONArray();
            for (int i = 0; i < mBookmarks.size(); i++) {
                JSONObject jsonBookmark = new JSONObject();
                JSONArray jsonCollections = new JSONArray();
                for (int j = 0; j < mBookmarks.get(i).mCollectionIndexs.size(); j++) {
                    jsonCollections.put(mBookmarks.get(i).mCollectionIndexs.get(j));
                }
                jsonBookmark.put("collection", jsonCollections);
                jsonBookmark.put("name", mBookmarks.get(i).mName);
                jsonBookmark.put("link", mBookmarks.get(i).mLink);
                jsonBookmark.put("type", pageTypeToString(mBookmarks.get(i).mPageType));
                jsonBookmarkArray.put(jsonBookmark);
            }
            bookmarksWithCollections.put("collections", jsonCollectionArray);
            bookmarksWithCollections.put("bookmarks", jsonBookmarkArray);
            jsonString = bookmarksWithCollections.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(filename, Context.MODE_PRIVATE));
            outputStreamWriter.write(jsonString);
            outputStreamWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
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

    public boolean addCollection(String name) {
        String link = name.toLowerCase().replace(" ", "_");
        if (findCollectionIndexByLink(link) >= 0) {
            return false;
        } else {
            BookmarkCollection collection = new BookmarkCollection(name, link);
            mCollections.add(collection);
            return true;
        }
    }

    public void removeCollection(String link) {
        mCollections.remove(findCollectionIndexByLink(link));
    }

    public int findCollectionIndexByLink(String link) {
        for (int i = 0; i < collectionsLength(); i++) {
            if (getCollectionLink(i).equals(link)) {
                return i;
            }
        }
        return -1;
    }

    public void addBookmark(List<String> collections, String name, String link, PageType type) {
        Bookmark bookmark = new Bookmark(collections, name, link, type);
        mBookmarks.add(bookmark);
    }
    public void addBookmark(String name, String link, PageType type) {
        Bookmark bookmark = new Bookmark(new ArrayList<String>(), name, link, type);
        mBookmarks.add(bookmark);
    }

    public void removeBookmark(String collection, String link) {
        int index = findBookmarkIndexByLink(link);
        Log.d("OaTS Index", Integer.toString(index));
        if (index != -1) {
            Log.d("OaTS", "Deleting Bookmark");
            if (mCollections.size() == 1 || collection.equals("_all_")) {
                mBookmarks.remove(index);
                Log.d("OaTS", "Deleting Bookmark Entirely");
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

    public List<String> getBookmarkCollections(int index) {
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
    public PageType getBookmarkType(int index) {
        return mBookmarks.get(index).mPageType;
    }
    public boolean bookmarkIsInCollection(String collectionLink, int index) {
        if (index < 0)
            return false;
        Bookmark bookmark = mBookmarks.get(index);
        for (int i = 0; i < bookmark.mCollectionIndexs.size(); i++) {
            if (mCollections.get(bookmark.mCollectionIndexs.get(i)).mLink.equals(collectionLink)) {
                return true;
            }
        }
        return false;
    }

    public void setBookmarkCollections(List<String> collectionLinks, int index) {
        mBookmarks.get(index).mCollectionIndexs.clear();
        mBookmarks.get(index).mCollectionIndexs.add(0);
        for (int i = 1; i < mCollections.size(); i++) {
            for (int j = 0; j < collectionLinks.size(); j++) {
                if (mCollections.get(i).mLink.equals(collectionLinks.get(j))) {
                    mBookmarks.get(index).mCollectionIndexs.add(i);
                }
            }
        }
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
