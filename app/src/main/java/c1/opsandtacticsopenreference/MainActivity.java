package c1.opsandtacticsopenreference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.io.InputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnActionExpandListener;
import android.view.MenuInflater;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.SearchView;
import android.support.v7.app.AppCompatActivity;
import android.support.graphics.drawable.VectorDrawableCompat;

import org.json.JSONException;
import org.json.JSONArray;

public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_MESSAGE = "c1.opsandtacticsopenrefernece.MESSAGE";
    public static final String EXTRA_MESSAGE2 = "c1.opsandtacticsopenrefernece.MESSAGE2";

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
    // Bookmarks
    DBHandler db;

    @Override
    @SuppressWarnings("ResourceType")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = DBHandler.getInstance(getApplicationContext());

        /*******************************************
         *              Danger Zone                *
         *******************************************/
        // This is for testing. This line of code will Nuke the bookmark database.
        // It will destroy all bookmarks and all bookmark collections.
        // Seriously. This line of code should not be used in production.
        // db.onUpgrade(db.getWritableDatabase(), 0, 0);
        /*******************************************
         *   You are now leaving the Danger Zone   *
         *******************************************/

        List<Collection> collections = db.getAllCollections();
        boolean default_exists = false;
        for (int i = 0; i < collections.size(); i++){
            if (collections.get(i).getCollection().equals("default")){
                default_exists = true;
                // Set default as the chosen category
                SharedPreferences settings = getSharedPreferences("bookmarkCollection", 0);
                SharedPreferences.Editor settings_editor = settings.edit();
                settings_editor.putString("bookmarkCollection", "default");
                settings_editor.apply();
                break;
            }
        }

        if (!default_exists) {

            // Creating a collection
            Collection collection = new Collection("default");
            long collection_id = db.addCollection(collection);
        }

        db.closeDB();
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
                String name = null;
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
                    case "add_collection":
                        addCollectionDialog();
                        break;
                    case "remove_collection":
                        removeCollectionDialog();
                        break;
                    default:
                }
                if (intent != null) {
                    page = listDataChild.get(
                                    listDataHeader.get(groupPosition)).get(
                                    childPosition).AssetLink();
                    name = listDataChild.get(
                                    listDataHeader.get(groupPosition)).get(
                                    childPosition).Text();
                    intent.putExtra(EXTRA_MESSAGE, page);
                    intent.putExtra(EXTRA_MESSAGE2, name);
                    startActivity(intent);
                }
                return false;
            }
        });
    }

    // Bookmark related stuff

    public void textDialog(String text){
        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.text_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        // set prompts.xml to alert dialog builder
        alertDialogBuilder.setView(promptsView);

        final TextView textView = (TextView) promptsView
                .findViewById(R.id.textView);

        textView.setText(text);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                // restart the add collection dialog
                                // the only dialog that calls this
                                addCollectionDialog();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    public void addCollection(String newCollectionName){
        db = DBHandler.getInstance(getApplicationContext());
        // Creating a collection
        // Remove any spaces that come before or after the input
        Pattern pattern = Pattern.compile("^\\s*(.*?)\\s*$");
        Matcher matcher = pattern.matcher(newCollectionName);
        if (matcher.find())
        {
            newCollectionName = matcher.group(1);
        } else {
            textDialog("Collection name cannot be blank!");
            return;
        }
        List<Collection> collections = db.getAllCollections();
        List<String> collections_string = new ArrayList<String>();
        // fill the string array with string from the collection array
        for (int i = 0; i < collections.size(); i++){
            collections_string.add(i, collections.get(i).getCollection());
        }
        // check to see if input is empty
        if (newCollectionName.isEmpty()){
            db.closeDB();
            textDialog("Collection name can't be blank!");
            return;
        }
        // check to see if a collection already exists
        if (collections_string.contains(newCollectionName)) {
            db.closeDB();
            textDialog("That collection already exists!");
            return;
        }
        // if everything checks out, add the collection
        Collection collection = new Collection(newCollectionName);
        long collection_id = db.addCollection(collection);
        db.closeDB();
        onResume();
    }

    public void addCollectionDialog(){
        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.add_collection, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        // set prompts.xml to alert dialog builder
        alertDialogBuilder.setView(promptsView);

        final EditText userInput = (EditText) promptsView
                .findViewById(R.id.editTextInput);

        // set dialog message
        alertDialogBuilder
                .setCancelable(true)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                // get user input and add it as a collection
                                addCollection(userInput.getText().toString());
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    public void removeCollection(int collection_id){
        db = DBHandler.getInstance(getApplicationContext());
        // Delete collection at specific ID
        db.deleteCollectionID(collection_id, true);
        db.closeDB();
        onResume();
    }

    public void removeCollectionDialog(){
        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.remove_collection, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        // set prompts.xml to alert dialog builder
        alertDialogBuilder.setView(promptsView);

        // Populate dialog with
        final RadioGroup radioGroup = (RadioGroup) promptsView
                .findViewById(R.id.radioGroup);
        List<Collection> collections = db.getAllCollections();
        RadioButton[] radioButtons = new RadioButton[collections.size()];
        for (int i = 0; i < collections.size(); i++) {
            radioButtons[i] = new RadioButton(this);
            radioButtons[i].setText(collections.get(i).getCollection());
            radioButtons[i].setId(i);
            if (i>0) {
                radioGroup.addView(radioButtons[i]);
            }
            Log.i("Collection Id",
                    radioButtons[i].getText() + String.valueOf(radioButtons[i].getId()));
        }

        // set dialog message
        alertDialogBuilder
                .setCancelable(true)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                // get user input and add it as a collection
                                int radio_id = radioGroup.getCheckedRadioButtonId()+1;
                                if (radio_id >= 0) {
                                    removeCollection(radio_id);
                                }
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        final AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();

        // make the button disabled...
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

        // ...until a radio button is selected
        for (int i = 1; i < radioButtons.length; i++) {
            radioButtons[i].setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }
            });
        }

        db.closeDB();
    }

    public void selectCollection(int collection_id){
        db = DBHandler.getInstance(getApplicationContext());
        String collection = db.getCollection(collection_id).getCollection();
        SharedPreferences settings = getSharedPreferences("bookmarkCollection", 0);
        SharedPreferences.Editor settings_editor = settings.edit();
        settings_editor.putString("bookmarkCollection", collection);
        settings_editor.apply();
        db.closeDB();
    }

    public void selectCollectionDialog(){
        // Settings
        SharedPreferences settings = getSharedPreferences("bookmarkCollection", 0);
        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.select_collection, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        // set prompts.xml to alert dialog builder
        alertDialogBuilder.setView(promptsView);
        // Populate dialog with
        final RadioGroup radioGroup = (RadioGroup) promptsView
                .findViewById(R.id.radioGroup);
        List<Collection> collections = db.getAllCollections();
        RadioButton[] radioButtons = new RadioButton[collections.size()];
        for (int i = 0; i < collections.size(); i++) {
            radioButtons[i] = new RadioButton(this);
            radioGroup.addView(radioButtons[i]);
            radioButtons[i].setText(collections.get(i).getCollection());
            radioButtons[i].setId(i);
            // set the radio button for the enabled collection true
            radioButtons[i].setChecked(false);
            if (collections.get(i).getCollection().equals(
                    settings.getString("bookmarkCollection", "default"))){
                radioButtons[i].setChecked(true);
            }
        }

        // set dialog message
        alertDialogBuilder
                .setCancelable(true)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                // get user input and add it as a collection
                                int radio_id = radioGroup.getCheckedRadioButtonId()+1;
                                if (radio_id >= 0) {
                                    selectCollection(radio_id);
                                }
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        final AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();

        db.closeDB();
    }

    // Menu related

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
/*
                Toast.makeText(
                        getApplicationContext(),
                        "Search", Toast.LENGTH_SHORT)
                        .show();
*/
                return true;

            case R.id.action_about:
                intent = new Intent(this, XMLActivity.class);
                page = new String("About.xml");
                intent.putExtra(EXTRA_MESSAGE, page);
                intent.putExtra(EXTRA_MESSAGE2, "About Ops and Tactics");
                startActivity(intent);
                return true;

            case R.id.action_bookmark_collection:
                selectCollectionDialog();
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
        List<Collection> allCollections = db.getAllCollections();
        List<TextAssetLink> bookmarks = listDataChild.get(listDataHeader.get(0));
        int i = 0;
        for (Collection collection : allCollections) {
            Log.d("Tag Name", collection.getCollection());
            TextAssetLink bookmark = new TextAssetLink(
                    collection.getCollection(),
                    "bookmarks/" + collection.getCollection(),
                    "list");
            bookmarks.add(i, bookmark);
            i++;
        }
        listDataChild.put(listDataHeader.get(0), bookmarks);
    }
}