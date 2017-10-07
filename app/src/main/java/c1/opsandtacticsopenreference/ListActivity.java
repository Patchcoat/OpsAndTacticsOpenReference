package c1.opsandtacticsopenreference;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View.OnLongClickListener;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ListActivity extends AppCompatActivity {

    public static final String EXTRA_MESSAGE = "c1.opsandtacticsopenrefernece.MESSAGE";
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
    private static final String ns = null;

    // Bookmarks
    Boolean bookmark;
    DBHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
    }

    @Override
    @SuppressWarnings("ResourceType")
    protected void onResume(){
        super.onResume();
        // setup Preferences
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ListActivity.this);
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

        // remove everything already in the view
        LinearLayout linLay = (LinearLayout) findViewById(R.id.parentLayout);
        linLay.removeAllViews();

        // Fill the page using the XML file pointed to
        // preparing list data
        try {
            parse(page);
        } catch (JSONException e) {
            e.printStackTrace();
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

    public void parse(String page) throws JSONException {

        linearLayout = (LinearLayout) findViewById(R.id.parentLayout);

        // JSON
        String jsonString;
        JSONArray listItems = new JSONArray();
        // List of items
        List<TextAssetLink> items = new ArrayList<TextAssetLink>();
        // check if reading from a file or bookmarks
        if (page.split("/")[0].equals("bookmarks")) {
            db = DBHandler.getInstance(getApplicationContext());
            items = db.getAllBookByCollectTAL(page.split("/")[1]);
            bookmark = true;
        } else {
            jsonString = loadJSONFromAsset(page);
            listItems = new JSONArray(jsonString);

            // iterate through and add items to the list
            int i;
            for (i = 0; i < listItems.length(); i++) {
                TextAssetLink item = new TextAssetLink(
                        listItems.getJSONObject(i).getString("text"),
                        listItems.getJSONObject(i).getString("link"),
                        listItems.getJSONObject(i).getString("type"));
                items.add(item);
                Log.i("Text",item.Text());
            }
            bookmark = false;
        }

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
        itemList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                Intent intent = null;
                switch(finalItems.get(position).AssetType()){
                    case "xml":
                        intent = new Intent(view.getContext(), XMLActivity.class);
                        break;
                    case "feat":
                        intent = new Intent(view.getContext(), FeatActivity.class);
                        break;
                    default:
                }
                if (intent != null) {
                    String page = new String(finalItems.get(position).AssetLink());
                    intent.putExtra(EXTRA_MESSAGE, page);
                    startActivity(intent);
                }
            }
        });
        itemList.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                           int position, long id) {
                switch(finalItems.get(position).AssetType()){
                    case "xml":
                        Toast.makeText(
                                getApplicationContext(),
                                "Long Click XML", Toast.LENGTH_SHORT)
                                .show();
                        break;
                    case "feat":
                        Toast.makeText(
                                getApplicationContext(),
                                "Long Click feat", Toast.LENGTH_SHORT)
                                .show();
                        break;
                    default:
                }
                return true;
            }
        });
        linearLayout.addView(itemList);
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
        // TODO make it so that if the page is bookmarked in the current collection the icon changes

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

            case R.id.action_search:
                onSearchRequested();
                return true;

            case R.id.action_about:
                intent = new Intent(this, XMLActivity.class);
                page = new String("About.xml");
                intent.putExtra(EXTRA_MESSAGE, page);
                startActivity(intent);
                return true;

            case R.id.action_bookmark_collection:
                Toast.makeText(
                        getApplicationContext(),
                        "Collection", Toast.LENGTH_SHORT)
                        .show();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }
}
