package c1.opsandtacticsopenreference;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
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
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.ArrayList;

public class FeatActivity extends AppCompatActivity {

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
    // Bookmarks
    String bookmarkCollection;
    String featLink;
    DBHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feat);
    }

    @Override
    @SuppressWarnings("ResourceType")
    protected void onResume() {
        super.onResume();
        // setup Preferences
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(FeatActivity.this);

        // Appearance Prefs
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

        // Change the background color of the most parent layout
        linearLayout = (LinearLayout) findViewById(R.id.TopLevelLayout);
        linearLayout.setBackgroundColor(background);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String feat = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);

        featLink = feat;
        SharedPreferences settings = getSharedPreferences("bookmarkCollection", 0);
        bookmarkCollection = settings.getString("bookmarkCollection", "default");

        String specificFeat = "";
        String featCategory = "";
        if (feat.contains("/")){
            featCategory = feat.split("/")[0];
            specificFeat = feat.split("(\\/| \\()")[1];
        } else {
            featCategory = feat;
        }

        // remove everything already in the view
        LinearLayout linLay = (LinearLayout) findViewById(R.id.parentLayout);
        linLay.removeAllViews();

        // Fill the page using the XML file pointed to
        // preparing list data
        try {
            parse(featCategory, specificFeat);
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

    public TextView createTextView (SpannableStringBuilder text){
        TextView textView = new TextView(this);
        textView.setTextSize(bodyTextSize);
        textView.setPadding(15,0,15,0); // Left, Top, Right, Bottom
        textView.setTextColor(textColor);
        Typeface font = Typeface.createFromAsset(
                getAssets(),
                "font/"+bodyFont);
        textView.setTypeface(font);
        textView.setText(text, TextView.BufferType.SPANNABLE);
        return textView;
    }

    public LinearLayout createBox (boolean tutoring, LinearLayout parent){
        LinearLayout boxOuter = new LinearLayout(this);
        LinearLayout boxInner;
        LinearLayout boxHeader;
        LinearLayout.LayoutParams boxOuterParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        boxOuterParams.setMargins(10,10,10,10);
        boxOuter.setLayoutParams(boxOuterParams);
        boxOuter.setOrientation(LinearLayout.VERTICAL);
        boxOuter.setBackgroundColor(boxBorder);
        boxOuter.setPadding(4,4,4,4);

        if (tutoring) {
            boxHeader = new LinearLayout(this);
            LinearLayout.LayoutParams boxHeaderParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            boxHeaderParams.setMargins(0, 0, 0, 0);
            boxHeader.setLayoutParams(boxHeaderParams);
            boxHeader.setOrientation(LinearLayout.VERTICAL);
            boxHeader.setBackgroundColor(0x00000000);
            boxHeader.setPadding(15, 3, 15, 5);
            boxOuter.addView(boxHeader);

            TextView headerView = new TextView(this);
            headerView.setTextSize(bodyTextSize);
            headerView.setPadding(15, 0, 15, 0); // Left, Top, Right, Bottom
            headerView.setTextColor(altText);
            Typeface font = Typeface.createFromAsset(
                    getAssets(),
                    "font/" + bodyFont);
            headerView.setTypeface(font);
            SpannableStringBuilder headerText = new SpannableStringBuilder("Tutoring");
            headerView.setText(headerText, TextView.BufferType.SPANNABLE);
            boxHeader.addView(headerView);
        }

        boxInner = new LinearLayout(this);
        LinearLayout.LayoutParams boxInnerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        boxInnerParams.setMargins(0,0,0,0);
        boxInner.setLayoutParams(boxInnerParams);
        boxInner.setOrientation(LinearLayout.VERTICAL);
        boxInner.setBackgroundColor(altBackground);
        boxInner.setPadding(15,5,15,5);
        boxOuter.addView(boxInner);
        parent.addView(boxOuter);

        return boxInner;
    }

    public void parse(String featCategory, String specificFeat) throws JSONException {

        linearLayout = (LinearLayout) findViewById(R.id.parentLayout);

        // Prerequisite Feats
        List<String> preFeats = new ArrayList<String>();

        LinearLayout topFeatLayout = new LinearLayout(this);
        LinearLayout.LayoutParams topFeatLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        topFeatLayout.setLayoutParams(topFeatLayoutParams);
        topFeatLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(topFeatLayout);

        // JSON
        String jsonString = loadJSONFromAsset("Feats/feats.json");
        JSONArray feats = new JSONArray(jsonString);

        String category = new String("");
        int cat = 0; // category number incrementer
        int fet = 0; // feat number incrementer
        boolean printCategory = false;
        // Populate list with feats from JSON file
        // This is horribly inefficient and should probably be improved
        for (int i = 0; i < feats.length(); i++){
            // Single String feat aspects
            final String localCategory = new String(feats.getJSONObject(i).getString("category"));
            final String name = new String(feats.getJSONObject(i).getString("name"));
            if (!category.equals(localCategory)) {
                category = localCategory;
                cat++;
                printCategory = true;
            } else {
                printCategory = false;
            }
            if (!featCategory.equals(localCategory) && !featCategory.equals("All")){
                continue;
            }
            String intro = new String(feats.getJSONObject(i).getString("intro"));
            String benefit = new String(feats.getJSONObject(i).getString("benefit"));
            String example = new String(feats.getJSONObject(i).getString("example"));
            String normal = new String(feats.getJSONObject(i).getString("normal"));
            String special = new String(feats.getJSONObject(i).getString("special"));
            // Array feat aspects
            JSONArray prerequisite = feats.getJSONObject(i).getJSONArray("prerequisite");
            if ((!specificFeat.isEmpty() && !specificFeat.equals(name)) ||
                (specificFeat.isEmpty() && !preFeats.isEmpty())){
                fet++;
                if (preFeats.isEmpty() || !preFeats.contains(name)){
                    continue;
                }
            } else if (!specificFeat.isEmpty() && specificFeat.equals(name)){
                if (prerequisite.length()>0 && preFeats.isEmpty()){
                    for (int j = 0; j < prerequisite.length() ; j++){
                        preFeats.add(prerequisite.get(j).toString().split("(\\/| \\()")[0]);
                    }
                }
            }
            String tutoringCat = new String(
                    feats.getJSONObject(i).getJSONObject("tutoring").getString("categories"));//Category
            String tutoringTime = new String(
                    feats.getJSONObject(i).getJSONObject("tutoring").getString("time"));//Time
            String tutoringBen = new String(
                    feats.getJSONObject(i).getJSONObject("tutoring").getString("benefit"));//Benefit
            // if print category is true
            // and if the focus is on a single feat, and the feat contains pre-feats
            // then print the category name
            if (printCategory && (specificFeat.isEmpty() &&
                     (preFeats.contains(name) || preFeats.isEmpty()))){
                TextView categoryText = new TextView(this);
                categoryText.setText(cat + "    " + localCategory);
                categoryText.setTextSize((float) (headerTextSize*0.75));
                categoryText.setPadding(15, 0,15,0);
                categoryText.setTextColor(textColor);
                categoryText.setTypeface(null, Typeface.BOLD);
                Typeface font = Typeface.createFromAsset(
                        getAssets(),
                        "font/"+bodyFont);
                categoryText.setTypeface(font);
                linearLayout.addView(categoryText);
                fet = 0;
            }

            LinearLayout featLayout = new LinearLayout(this);
            LinearLayout.LayoutParams featLayoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            featLayout.setLayoutParams(featLayoutParams);
            featLayout.setOrientation(LinearLayout.VERTICAL);
            SpannableStringBuilder text;
            // Name
            if (!name.isEmpty()){
                fet ++;
                TextView nameText = new TextView(this);
                nameText.setText(cat + "." + fet + "   " + name);
                nameText.setTextSize((float) (headerTextSize*0.625));
                nameText.setPadding(15, 0,15,0);
                nameText.setTextColor(textColor);
                nameText.setTypeface(null, Typeface.BOLD);
                Typeface font = Typeface.createFromAsset(
                        getAssets(),
                        "font/"+bodyFont);
                nameText.setTypeface(font);
                featLayout.addView(nameText);
            }
            // Intro
            if (!intro.isEmpty()){
                text = new SpannableStringBuilder(intro);
                featLayout.addView(createTextView(text));
            }
            // Prerequisite
            if (prerequisite.length() > 0){
                String prerequisites = "";
                for (int j = 0; j < prerequisite.length(); j++){
                    prerequisites += prerequisite.get(j).toString();
                    if (j < prerequisite.length()-1){
                        prerequisites += ", ";
                    }
                }
                text = new SpannableStringBuilder("   Prerequisite" +
                        (prerequisite.length() > 1 ? "s" : "") + " " + prerequisites);
                text.setSpan(
                        new android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                        0, 16, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                featLayout.addView(createTextView(text));
            }
            // Benefit
            if (!benefit.isEmpty()){
                text = new SpannableStringBuilder("   Benefit "+ benefit);
                text.setSpan(
                        new android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                        0, 10, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                featLayout.addView(createTextView(text));
            }
            // Example
            if (!example.isEmpty()){
                LinearLayout boxInner = createBox (false, featLayout);
                text = new SpannableStringBuilder("Example: " + example);
                text.setSpan(
                        new android.text.style.StyleSpan(android.graphics.Typeface.ITALIC),
                        8, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                boxInner.addView(createTextView(text));
            }
            // Normal
            if (!normal.isEmpty()){
                text = new SpannableStringBuilder("   Normal "+ normal);
                text.setSpan(
                        new android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                        0, 8, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                featLayout.addView(createTextView(text));
            }
            // Special
            if (!special.isEmpty()){
                text = new SpannableStringBuilder("   Special "+ special);
                text.setSpan(
                        new android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                        0, 11, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                featLayout.addView(createTextView(text));
            }
            // Tutoring
            if (!tutoringBen.isEmpty()){
                LinearLayout boxInner = createBox (true, featLayout);

                if (!tutoringCat.isEmpty()){
                    text = new SpannableStringBuilder("Categories " + tutoringCat);
                    text.setSpan(
                            new android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                            0, 10, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    boxInner.addView(createTextView(text));
                }
                if (!tutoringTime.isEmpty()){
                    text = new SpannableStringBuilder("Time " + tutoringTime);
                    text.setSpan(
                            new android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                            0, 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    boxInner.addView(createTextView(text));
                }
                text = new SpannableStringBuilder("Benefit " + tutoringBen);
                text.setSpan(
                        new android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                        0, 7, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                boxInner.addView(createTextView(text));
            }
            // For when displaying a specific feat
            if (!specificFeat.isEmpty() && specificFeat.equals(name)){
                topFeatLayout.addView(featLayout);
                if (!preFeats.isEmpty()) {
                    // Text
                    TextView prereqFeatText = new TextView(this);
                    prereqFeatText.setText("Prerequisite Feat" + (prerequisite.length() > 1 ? "s" : ""));
                    prereqFeatText.setTextSize((float) (headerTextSize));
                    prereqFeatText.setPadding(15, 0,15,0);
                    prereqFeatText.setTextColor(textColor);
                    prereqFeatText.setTypeface(null, Typeface.BOLD);
                    Typeface font = Typeface.createFromAsset(
                            getAssets(),
                            "font/"+bodyFont);
                    prereqFeatText.setTypeface(font);
                    topFeatLayout.addView(prereqFeatText);
                    // Reset everything to a value that makes it all work in the end
                    specificFeat = "";
                    i = -1;
                    cat = 0;
                    fet = -1;
                    featCategory = "All";
                    category = "";
                } else {
                    break;
                }
            } else {
                Log.i("Link", localCategory + "/" + name);
                featLayout.setClickable(true);
                featLayout.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Intent intent = new Intent(v.getContext(), FeatActivity.class);
                        String page = localCategory + "/" + name;
                        intent.putExtra(EXTRA_MESSAGE, page);
                        startActivity(intent);
                    }
                });
                linearLayout.addView(featLayout);
            }
        }
    }

    private void createBookmark(){
        Log.i("Bookmark Collection", bookmarkCollection);
        Log.i("Bookmark Link", featLink);
        String bookmarkName = new String();
        if (featLink.contains("/")){
            bookmarkName = featLink.split("/")[1];
        } else {
            bookmarkName = featLink;
        }
        Log.i("Bookmark Name", bookmarkName);

        db = DBHandler.getInstance(getApplicationContext());

        List<Collection> collections = db.getAllCollections();
        long collection_id = 1;
        for (int i = 0; i < collections.size(); i++){
            Log.i("Collection", collections.get(i).getCollection());
            if (collections.get(i).getCollection().equals(bookmarkCollection)){
                collection_id = collections.get(i).getId();
                break;
            }
        }

        Bookmark bookmark = new Bookmark(bookmarkName, featLink, "feat");
        long bookmark_id = db.addBookmark(bookmark, new long[]{collection_id});

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.content_menu, menu);

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

            case R.id.action_bookmark:
                createBookmark();
                Toast.makeText(
                        getApplicationContext(),
                        "Bookmark", Toast.LENGTH_SHORT)
                        .show();
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
