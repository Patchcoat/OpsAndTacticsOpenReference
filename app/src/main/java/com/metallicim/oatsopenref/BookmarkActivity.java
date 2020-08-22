package com.metallicim.oatsopenref;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.LinearLayout;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BookmarkActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "com.metallicim.oatsopenref.MESSAGE";

    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private String pageLink;

    private int mThemeID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mThemeID = setTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmark);

        // Setup message from start intent
        Intent intent = getIntent();
        pageLink = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);

        // setup toolbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // remove everything already in the view
        LinearLayout layout = findViewById(R.id.linear_layout);

        // Set the Theme
        int themeID = setTheme();
        if (mThemeID != themeID) {
            this.recreate();
        }

        // set up the recycler view
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        // Fill the page from bookmarks
        Bookmarks bookmarks = Bookmarks.getInstance();
        List<Bookmarks.Bookmark> bookmarkList = new ArrayList<>();
        for (int i = 0; i < bookmarks.bookmarksLength(); i++) {
            // add the bookmark if it's category matches the page link, or if the page link includes all bookmarks
            if (bookmarks.getBookmarkCollection(i).equals(pageLink) || pageLink.equals("_all_")) {
                Bookmarks.Bookmark bookmark = new Bookmarks.Bookmark(
                        bookmarks.getBookmarkCollection(i),
                        bookmarks.getBookmarkName(i),
                        bookmarks.getBookmarkLink(i));
                Log.d("OaTS Collection", bookmarks.getBookmarkCollection(i));
                Log.d("OaTS Name", bookmarks.getBookmarkName(i));
                Log.d("OaTS Link", bookmarks.getBookmarkLink(i));
                bookmarkList.add(bookmark);
            }
        }

        mAdapter = new BookmarksAdapter(bookmarkList);
        recyclerView.setAdapter(mAdapter);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch(item.getItemId()) {
            case android.R.id.home:
                finish();
                return super.onOptionsItemSelected(item);
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
}