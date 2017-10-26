package c1.opsandtacticsopenreference;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Searchable extends AppCompatActivity {

    public static final String EXTRA_MESSAGE = "c1.opsandtacticsopenrefernece.MESSAGE";
    public static final String EXTRA_MESSAGE2 = "c1.opsandtacticsopenrefernece.MESSAGE2";

    LinearLayout linearLayout = null;
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

    // Search Database
    DataBaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchable);

        db = new DataBaseHelper(this);

        try {

            db.createDataBase();

        } catch (IOException ioe) {

            throw new Error("Unable to create database");

        }

        try {

            db.openDataBase();

        }catch(SQLException sqle){

            throw sqle;
        }
    }

    @Override
    @SuppressWarnings("ResourceType")
    protected void onResume(){
        super.onResume();
        // setup Preferences
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(c1.opsandtacticsopenreference.Searchable.this);
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

        ta.recycle();

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
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String page = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);

        // Change the background color of the most parent layout
        linearLayout = (LinearLayout) findViewById(R.id.TopLevelLayout);
        linearLayout.setBackgroundColor(altBackground);

        // Get the intent, verify the action and get the query
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Search(query);
        }
    }

    // preform the search
    private void Search(String query){
        Log.i("Search", query);
        Cursor cursor = db.search(query);
        List<TextAssetLink> items = new ArrayList<>();
        // Only process a non-null cursor with rows.
        if (cursor != null && cursor.getCount() > 0) {
            // You must move the cursor to the first item.
            cursor.moveToFirst();
            int nameIndex;
            int linkIndex;
            int typeIndex;
            String nameResult;
            String linkResult;
            String typeResult;
            // Iterate over the cursor, while there are entries.
            do {
                // Don't guess at the column index.
                // Get the index for the named column.
                nameIndex = cursor.getColumnIndex("page_name");
                linkIndex = cursor.getColumnIndex("page_link");
                typeIndex = cursor.getColumnIndex("page_type");
                // Get the value from the column for the current cursor.
                nameResult = cursor.getString(nameIndex);
                linkResult = cursor.getString(linkIndex);
                typeResult = cursor.getString(typeIndex);
                // Add result to what's already in the text view.
                Log.i("Name", nameResult);
                Log.i("Link", linkResult);
                Log.i("Type", typeResult);
                items.add(new TextAssetLink(nameResult, linkResult, typeResult));
            } while (cursor.moveToNext()); // Returns true or false
            cursor.close();
        } else {
            // Do nothing
        }
        db.close();

        linearLayout = (LinearLayout) findViewById(R.id.parentLayout);

        // List View
        final ListView itemList = new ListView(this);

        // because the custom adapter works with an Array list but not a List
        ArrayList<TextAssetLink> stringArray = new ArrayList<TextAssetLink>(items);

        // formatting
        String fontFormat = bodyFont;
        Typeface font = Typeface.createFromAsset(
                getAssets(),
                "font/"+fontFormat);
        CustomListAdapter modeListAdapter = new CustomListAdapter(this, stringArray,
                font, textColor, background);
        itemList.setAdapter(modeListAdapter);

        // Make items in the list clickable
        final List<TextAssetLink> finalItems = items;
        itemList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                Intent intent = null;
                switch(finalItems.get(position).AssetType()){
                    case "xml":
                        intent = new Intent(view.getContext(), XMLActivity.class);
                        break;
                    case "list":
                        intent = new Intent(view.getContext(), c1.opsandtacticsopenreference.ListActivity.class);
                        break;
                    case "feat":
                        intent = new Intent(view.getContext(), FeatActivity.class);
                        break;
                    default:
                }
                if (intent != null) {
                    String page = new String(finalItems.get(position).AssetLink());
                    String name = new String(finalItems.get(position).Text());
                    intent.putExtra(EXTRA_MESSAGE, page);
                    intent.putExtra(EXTRA_MESSAGE2, name);
                    startActivity(intent);
                }
            }
        });
        linearLayout.addView(itemList);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu, menu);

        // set regular icon tint
        for(int i = 0; i < menu.size(); i++){
            Drawable drawable = menu.getItem(i).getIcon();
            if(drawable != null) {
                drawable.mutate();
                drawable.setColorFilter(textColor, PorterDuff.Mode.SRC_ATOP);
            }
        }
        Drawable upArrow;

        // set back arrow tint
        upArrow = VectorDrawableCompat.create(getResources(), R.drawable.ic_arrow_back_black_24dp, null);
        upArrow.setColorFilter(textColor, PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);

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

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }
}
