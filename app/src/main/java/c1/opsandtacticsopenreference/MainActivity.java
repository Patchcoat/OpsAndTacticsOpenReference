package c1.opsandtacticsopenreference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.io.InputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnActionExpandListener;
import android.view.MenuInflater;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.EditText;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.SearchView;
import android.support.v7.app.AppCompatActivity;
import android.support.graphics.drawable.VectorDrawableCompat;

import org.json.JSONException;
import org.json.JSONArray;

public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_MESSAGE = "c1.opsandtacticsopenrefernece.MESSAGE";

    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<TextAssetLink> listDataHeader;
    HashMap<TextAssetLink, List<TextAssetLink>> listDataChild;

    String bodyFont;
    String secondaryFont;
    int headerTextSize;
    int bodyTextSize;
    // Theme
    int textColor;
    int altText;
    int background;
    int altBackground;
    int tableBorder;
    int boxBorder;
    // Books
    ArrayList<String> ruleBooks = new ArrayList<String>();

    @Override
    @SuppressWarnings("ResourceType")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    @SuppressWarnings("ResourceType")
    protected void onResume(){
        super.onResume();

        // setup Preferences
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        String headerTextSizeString = sharedPref.getString("header_size", "");
        String bodyTextSizeString = sharedPref.getString("font_size", "");
        String theme = sharedPref.getString("theme","");
        bodyFont = sharedPref.getString("body_font", "");
        secondaryFont = sharedPref.getString("secondary_font", "");
        headerTextSize = 40;
        bodyTextSize = 13;
        if (headerTextSizeString != ""){
            headerTextSize = Integer.parseInt(headerTextSizeString);
        }
        if (bodyTextSizeString != ""){
            bodyTextSize = Integer.parseInt(bodyTextSizeString);
        }

        int[] attrs = {
                android.R.attr.textColorPrimary,
                android.R.attr.textColorSecondary,
                android.R.attr.background,
                android.R.attr.shadowColor,
                android.R.attr.color,
                android.R.attr.keyTextColor};
        TypedArray ta;
        // Parse Style
        switch(theme){
            case "standard":
                ta = getTheme().obtainStyledAttributes(R.style.standard, attrs);
                break;
            case "dark":
                ta = getTheme().obtainStyledAttributes(R.style.dark, attrs);
                break;
            case "low_contrast":
                ta = getTheme().obtainStyledAttributes(R.style.low_contrast, attrs);
                break;
            case "low_contrast_dark":
                ta = getTheme().obtainStyledAttributes(R.style.low_contrast_dark, attrs);
                break;
            case "solarized_light":
                ta = getTheme().obtainStyledAttributes(R.style.solarized_light, attrs);
                break;
            case "solarized_dark":
                ta = getTheme().obtainStyledAttributes(R.style.solarized_dark, attrs);
                break;
            case "terminal":
                ta = getTheme().obtainStyledAttributes(R.style.terminal, attrs);
                break;
            default:
                ta = getTheme().obtainStyledAttributes(R.style.standard, attrs);
        }

        textColor = ta.getColor(0, Color.WHITE);
        altText = ta.getColor(1, Color.BLACK);
        background = ta.getColor(2, Color.WHITE);
        altBackground = ta.getColor(3, Color.LTGRAY);
        tableBorder = ta.getColor(4, Color.DKGRAY);
        boxBorder = ta.getColor(5, Color.WHITE);

        String fontFormat = bodyFont;
        Typeface font = Typeface.createFromAsset(
                getAssets(),
                "font/"+fontFormat);
        fontFormat = new StringBuffer(fontFormat).insert(
                fontFormat.length()-4, "_bold").toString();
        Typeface boldFont = Typeface.createFromAsset(
                getAssets(),
                "font/"+fontFormat);

        ta.recycle();

        LinearLayout parentLayout = (LinearLayout) findViewById(R.id.parentLayout);
        parentLayout.setBackgroundColor(altBackground);
        ExpandableListView expandableListView = (ExpandableListView) findViewById(R.id.lvExp);
        expandableListView.setGroupIndicator(null);

        // setup action bar
        Toolbar bar = (Toolbar) findViewById(R.id.toolbar);
        bar.setBackgroundColor(altBackground);
        bar.setTitleTextColor(textColor);
        // Set overflow icon color tint
        final Drawable overflowIcon = bar.getOverflowIcon();
        if (overflowIcon != null){
            Drawable wrapDrawable = DrawableCompat.wrap(overflowIcon);
            DrawableCompat.setTint(wrapDrawable, textColor);
            DrawableCompat.setTintMode(wrapDrawable, PorterDuff.Mode.SRC_IN);
            bar.setOverflowIcon(wrapDrawable);
        }
        // Set the action bar
        setSupportActionBar(bar);

        // Get books
        String[] bookTitles = {
                "core_rulebook",
                "simplified_tactics",
                "advanced_arms",
                "modern_magika",
                "field_identification_guide",
                "procedural_weapons"};
        ruleBooks.clear();
        ruleBooks.add("bookmarks");
        for (int i = 0; i < bookTitles.length; i++){
            if (sharedPref.getBoolean(bookTitles[i], false)){
                ruleBooks.add(bookTitles[i]);
            }
        }

        // get the listview
        expListView = (ExpandableListView) findViewById(R.id.lvExp);

        // preparing list data
        try {
            prepareListData();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        listAdapter = new ExpandableListAdapter(
                this, listDataHeader, listDataChild, font, boldFont, textColor, altText,
                background, altBackground);

        // setting list adapter
        expListView.setAdapter(listAdapter);

        // Listview on child click listener
        expListView.setOnChildClickListener(new OnChildClickListener() {

            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                Intent intent = null;
                String page = null;
                switch(listDataChild.get(
                        listDataHeader.get(groupPosition)).get(
                        childPosition).AssetType()){
                    case "xml":
                        intent = new Intent(v.getContext(), XMLActivity.class);
                        break;
                    case "feat":
                        intent = new Intent(v.getContext(), FeatActivity.class);
                        break;
                    case "list":
                        intent = new Intent(v.getContext(), ListActivity.class);
                        break;
                    default:
                }
                if (intent != null) {
                    page = new String(
                            listDataChild.get(
                                    listDataHeader.get(groupPosition)).get(
                                    childPosition).AssetLink());
                    intent.putExtra(EXTRA_MESSAGE, page);
                    startActivity(intent);
                }
                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        // set regular icon tint
        for(int i = 0; i < menu.size(); i++){
            Drawable drawable = menu.getItem(i).getIcon();
            if(drawable != null) {
                drawable.mutate();
                drawable.setColorFilter(textColor, PorterDuff.Mode.SRC_ATOP);
            }
        }
        Drawable upArrow;

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        String page;
        switch (item.getItemId()) {
            case R.id.action_settings:
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;

            case R.id.action_search:
                onSearchRequested();
//                Toast.makeText(
//                        getApplicationContext(),
//                        "Search", Toast.LENGTH_SHORT)
//                        .show();
                return true;

            case R.id.action_about:
                intent = new Intent(this, XMLActivity.class);
                page = new String("About.xml");
                intent.putExtra(EXTRA_MESSAGE, page);
                startActivity(intent);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    // Read json file
    public String loadJSONFromAsset(String file) {
        String json = null;
        try {

            InputStream is = getAssets().open(file);

            int size = is.available();

            byte[] buffer = new byte[size];

            is.read(buffer);

            is.close();

            json = new String(buffer, "UTF-8");


        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;

    }

    /*
     * Preparing the list data
     */
    private void prepareListData() throws JSONException {
        listDataHeader = new ArrayList<TextAssetLink>();
        listDataChild = new HashMap<TextAssetLink, List<TextAssetLink>>();

        String jsonString = loadJSONFromAsset("Contents.json");

        JSONArray chapters = new JSONArray(jsonString);

        int k = 0;
        // Populate list with chapters from json file
        for (int i = 0; i < chapters.length(); i++){
            List<TextAssetLink> children = new ArrayList<TextAssetLink>();
            TextAssetLink book = new TextAssetLink(
                    chapters.getJSONObject(i).getString("book"), "", "");
            if (ruleBooks.contains(book.Text())){
                TextAssetLink chapter = new TextAssetLink(
                        chapters.getJSONObject(i).getString("text"), "", "");
                listDataHeader.add(chapter);
                JSONArray chapterChildren = chapters.getJSONObject(i).getJSONArray("children");
                // Add sub sections to chapters
                for (int j = 0; j < chapterChildren.length(); j++){
                    TextAssetLink child = new TextAssetLink(
                            chapterChildren.getJSONObject(j).getString("text"),
                            chapterChildren.getJSONObject(j).getString("link"),
                            chapterChildren.getJSONObject(j).getString("type"));
                    children.add(child);
                }
                listDataChild.put(listDataHeader.get(k), children);
                k++;
            }
        }

        // Populate bookmarks
        jsonString = loadJSONFromAsset("Bookmarks.json");
        JSONArray jsonBookmarks = new JSONArray(jsonString);
        List<TextAssetLink> bookmarks = listDataChild.get(listDataHeader.get(0));
        for (int i = 0; i < jsonBookmarks.length(); i++){
            TextAssetLink bookmark = new TextAssetLink(
                    jsonBookmarks.getJSONObject(i).getString("name"),
                    jsonBookmarks.getJSONObject(i).getString("link"),
                    jsonBookmarks.getJSONObject(i).getString("type"));
            bookmarks.add(i, bookmark);
        }
        listDataChild.put(listDataHeader.get(0), bookmarks);
    }
}