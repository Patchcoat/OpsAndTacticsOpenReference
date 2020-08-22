package com.metallicim.oatsopenref;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_MESSAGE = "com.metallicim.oatsopenref.MESSAGE";

    int mThemeID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mThemeID = setTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // setup toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // load bookmarks
        Bookmarks bookmarks = Bookmarks.getInstance();
        try {
            bookmarks.readFile(this);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        bookmarks.addBookmark("_all_", "Hello", "hello");

        for (int i = 0; i < bookmarks.bookmarksLength(); i++) {
            Log.d("OaTS Collection", bookmarks.getBookmarkCollection(i));
            Log.d("OaTS Name", bookmarks.getBookmarkName(i));
            Log.d("OaTS Link", bookmarks.getBookmarkLink(i));
        }

        // load contents
        JSONArray contents = new JSONArray();
        try {
            contents = loadJSONFromAsset(getApplicationContext());
            // load bookmark categories into contents
            JSONObject jsonBookmarks = (JSONObject) contents.get(0);
            JSONArray jsonBookmarkCollections = jsonBookmarks.getJSONArray("children");
            for (int i = 0; i < bookmarks.collectionsLength(); i++) {
                JSONObject jsonCollection = new JSONObject();
                jsonCollection.put("text", bookmarks.getCollectionName(i));
                jsonCollection.put("link", bookmarks.getCollectionLink(i));
                jsonCollection.put("type", "bookmark_collection");
                jsonBookmarkCollections.put(jsonCollection);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // fill recycler view
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        List<Contents> contentsList = new ArrayList<>();
        try {
            for (int i = 0; i < contents.length(); i++) {
                JSONObject contentsObj = contents.getJSONObject(i);
                List<Category> categories = new ArrayList<>();
                for (int j = 0; j < contentsObj.getJSONArray("children").length(); j++) {
                    JSONObject categoryObj = contentsObj.getJSONArray("children").getJSONObject(j);
                    Category category = new Category(this,
                                                     categoryObj.getString("text"),
                                                     categoryObj.getString("link"),
                                                     categoryObj.getString("type"));
                    categories.add(category);
                }
                Contents content = new Contents(contentsObj.getString("text"), categories);
                contentsList.add(content);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ContentsAdapter mAdapter = new ContentsAdapter(getApplicationContext(), contentsList, getTheme());
        recyclerView.setAdapter(mAdapter);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
    }

    @Override
    public void onResume() {
        super.onResume();
        int themeID = setTheme();
        if (mThemeID != themeID) {
            this.recreate();
        }
    }

    private int setTheme() {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        String themeColor = sharedPreferences.getString("color", "");
        ParseTheme parseTheme = new ParseTheme();
        int themeID = parseTheme.parseThemeColor(themeColor);
        super.setTheme(themeID);

        return themeID;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch(item.getItemId()) {
            case R.id.action_about:
                intent = new Intent(this, XMLActivity.class);
                String message = "About.xml";
                intent.putExtra(EXTRA_MESSAGE, message);
                startActivity(intent);
                return true;
            case R.id.action_settings:
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public JSONArray loadJSONFromAsset(Context context) throws JSONException, IOException {
        String json;
        InputStream is = context.getAssets().open("Contents.json");
        byte[] buffer = new byte[is.available()];
        is.read(buffer);
        is.close();
        json = new String(buffer, "UTF-8");
        return new JSONArray(json);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
}