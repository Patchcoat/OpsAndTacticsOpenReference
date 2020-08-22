package com.metallicim.oatsopenref;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

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

public class BookmarkActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "com.metallicim.oatsopenref.MESSAGE";

    String pageLink;

    int mThemeID;

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

        // Fill the page from bookmarks
        // TODO fill the recycler view
        Bookmarks bookmarks = Bookmarks.getInstance();
        for (int i = 0; i < bookmarks.bookmarksLength(); i++) {
            // add the bookmark if it's category matches the page link, or if the page link includes all bookmarks
            if (bookmarks.getBookmarkCollection(i).equals(pageLink) || pageLink.equals("_all_")) {
                Log.d("OaTS Collection", bookmarks.getBookmarkCollection(i));
                Log.d("OaTS Name", bookmarks.getBookmarkName(i));
                Log.d("OaTS Link", bookmarks.getBookmarkLink(i));
            }
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