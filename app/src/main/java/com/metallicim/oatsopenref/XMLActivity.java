package com.metallicim.oatsopenref;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.util.Log;
import android.util.TypedValue;
import android.util.Xml;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class XMLActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "com.metallicim.oatsopenref.MESSAGE";
    public static final String EXTRA_MESSAGE_NAME = "com.metallicim.oatsopenref.MESSAGE_NAME";

    String pageLink;
    String pageName;
    private static final String ns = null;
    LinearLayout container;
    Menu mMenu;

    int mThemeID;
    Bookmarks mBookmarks;

    int headerTextSize = 40;
    int boxBorder;
    int boxHeaderTextColor;
    int boxInnerBackgroundColor;
    int tableAltBackground;
    int tableHeaderBackground;
    int tableHeaderTextColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mThemeID = setTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xml);

        // Setup message from start intent
        Intent intent = getIntent();
        pageLink = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        pageName = intent.getStringExtra(MainActivity.EXTRA_MESSAGE_NAME);

        // setup toolbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // setup parent container that everything goes in
        container = (LinearLayout) findViewById(R.id.linear_layout);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // get bookmarks
        mBookmarks = Bookmarks.getInstance();

        // remove everything already in the view
        LinearLayout layout = findViewById(R.id.linear_layout);

        // Fill the page using the XML file pointed to
        try {
            parse(pageLink);
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

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

        Resources.Theme theme = this.getTheme();
        theme.applyStyle(themeID, true);
        TypedValue color = new TypedValue();
        theme.resolveAttribute(android.R.attr.color, color, true);
        boxBorder = color.data;
        tableHeaderBackground = color.data;
        theme.resolveAttribute(android.R.attr.centerColor, color, true);
        boxHeaderTextColor = color.data;
        boxInnerBackgroundColor = color.data;
        tableHeaderTextColor = color.data;
        theme.resolveAttribute(android.R.attr.shadowColor, color, true);
        tableAltBackground = color.data;

        return themeID;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.content_menu, menu);
        mMenu = menu;
        if (mBookmarks.isBookmarked(pageLink)) {
            mMenu.findItem(R.id.action_bookmark).setIcon(R.drawable.ic_bookmark_24dp);
        } else {
            mMenu.findItem(R.id.action_bookmark).setIcon(R.drawable.ic_bookmark_border_24dp);
        }
        return true;
    }


    private AlertDialog askForCollection() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.remove_collections);
        final List<String> selectedItems = new ArrayList<>();
        final Bookmarks bookmarks = Bookmarks.getInstance();
        int length = bookmarks.collectionsLength() - 1;
        CharSequence[] items = new CharSequence[length];
        CharSequence[] links = new CharSequence[length];
        boolean[] checkedItems = new boolean[length];
        for (int i = 0; i < length; i++) {
            items[i] = bookmarks.getCollectionName(i+1);
            links[i] = bookmarks.getCollectionLink(i+1);
            checkedItems[i] = bookmarks.bookmarkIsInCollection(links[i].toString(),
                    bookmarks.findBookmarkIndexByLink(pageLink));
            if (checkedItems[i]) {
                selectedItems.add(links[i].toString());
            }
        }
        final CharSequence[] finalLinks = links;
        builder.setMultiChoiceItems(items, checkedItems,
                new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        CharSequence link = finalLinks[which];
                        if (isChecked) {
                            selectedItems.add(link.toString());
                        } else selectedItems.remove(link.toString());
                    }
                });

        final Context context = this;
        final int index = bookmarks.findBookmarkIndexByLink(pageLink);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                // user clicked ok
                if (index >= 0) { // if the bookmark exists
                    bookmarks.setBookmarkCollections(selectedItems, index);
                } else { // if the bookmark doesn't exist
                    bookmarks.addBookmark(selectedItems, pageName, pageLink, PageType.feat);
                }
                mMenu.findItem(R.id.action_bookmark).setIcon(R.drawable.ic_bookmark_24dp);
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // user clicked cancel
            }
        });
        builder.setNeutralButton(R.string.delete_bookmark, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // user deletes bookmark
                bookmarks.removeBookmark("_all_", pageLink);
                mMenu.findItem(R.id.action_bookmark).setIcon(R.drawable.ic_bookmark_border_24dp);
            }
        });

        return builder.create();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch(item.getItemId()) {
            case android.R.id.home:
                finish();
                super.onBackPressed();
                return super.onOptionsItemSelected(item);
            case R.id.action_about:
                intent = new Intent(this, XMLActivity.class);
                String message = "About.xml";
                intent.putExtra(EXTRA_MESSAGE, message);
                intent.putExtra(EXTRA_MESSAGE_NAME, "About");
                startActivity(intent);
                return true;
            case R.id.action_settings:
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_bookmark:
                if (mBookmarks.collectionsLength() > 1) {
                    // ask for collection
                    askForCollection().show();
                } else {
                    if (!mBookmarks.isBookmarked(pageLink)) {
                        mBookmarks.addBookmark(pageName, pageLink, PageType.XML);
                        mMenu.findItem(R.id.action_bookmark).setIcon(R.drawable.ic_bookmark_24dp);
                    } else {
                        mBookmarks.removeBookmark("_all_", pageLink);
                        mMenu.findItem(R.id.action_bookmark).setIcon(R.drawable.ic_bookmark_border_24dp);
                    }
                    mBookmarks.updateFile(this);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void parse(String file) throws XmlPullParserException, IOException {
        AssetManager assetManager = getAssets();
        InputStream inputStream = null;
        try {
            inputStream = assetManager.open(file);
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(inputStream, null);
            parser.nextTag();
            parser.require(XmlPullParser.START_TAG, ns, "body");
            while(parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                String name = parser.getName();
                if (name.equals("item")) {
                    readItem(parser);
                } else {
                    skip(parser);
                }
            }
        } finally {
            inputStream.close();
        }
    }

    private void readItem(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "item");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            switch (name) {
                case "header":
                    container.addView(readHeader(parser));
                    break;
                case "text":
                    container.addView(readTextTag(parser));
                    break;
                case "box":
                    container.addView(readBox(parser));
                    break;
                case "table":
                    container.addView(readTable(parser));
                    break;
                case "list":
                    container.addView(readList(parser));
                    break;
                default:
                    skip(parser);
                    break;
            }
        }
    }

    private int textViewGravity(String textAlign) {
        if (textAlign == null)
            return Gravity.NO_GRAVITY;
        switch (textAlign) {
            case "left":
                return Gravity.START;
            case "center":
                return Gravity.CENTER_HORIZONTAL;
            default:
                return Gravity.END;// includes the case for align right
        }
    }

    private float headerTextSize(String level) {
        float textSize = headerTextSize;
        if (level != null && !level.isEmpty() &&
                level.matches("^-?\\d+$") && Float.parseFloat(level) >= 0){
            switch (level){
                case "0":
                    textSize = textSize*1;
                    break;
                case "1":
                    textSize = (float) (textSize*0.75);
                    break;
                case "2":
                    textSize = (float) (textSize*0.625);
                    break;
                case "3":
                    textSize = (float) (textSize*0.5);
                    break;
                case "4":
                    textSize = (float) (textSize*0.375);
                    break;
                case "5":
                    textSize = (float) (textSize*0.325);
                    break;
                default:
                    textSize = 13;
            }
        }
        return textSize;
    }

    private TextView readHeader(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "header");
        String level = parser.getAttributeValue(null, "level");
        String textAlign = parser.getAttributeValue(null, "textAlign");
        SpannableStringBuilder header = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "header");

        TextView textView = new TextView(this);
        textView.setText(header);
        textView.setTextSize(headerTextSize(level));
        textView.setPadding(15, 0,15,0);
        textView.setTypeface(null, Typeface.BOLD);
        // the default gravity for a header is END, not NO_GRAVITY
        int gravity = textViewGravity(textAlign);
        textView.setGravity(gravity == Gravity.NO_GRAVITY ? Gravity.END : gravity);

        return textView;
    }

    private TextView readTextTag(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "text");
        String textAlign = parser.getAttributeValue(null, "textAlign");
        SpannableStringBuilder text = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "text");

        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setGravity(textViewGravity(textAlign));

        textView.setPadding(15,0,15,0);

        return textView;
    }

    private LinearLayout newBox(Context context, int color) {
        LinearLayout box = new LinearLayout(context);
        LinearLayout.LayoutParams boxParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        boxParams.setMargins(0, 0, 0, 0);
        box.setLayoutParams(boxParams);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setBackgroundColor(color);
        return box;
    }

    private LinearLayout readBox(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "box");
        LinearLayout boxOuter = new LinearLayout(this);
        LinearLayout boxHeader = newBox(this, boxBorder);
        LinearLayout boxInner = newBox(this, boxInnerBackgroundColor);
        boxInner.setPadding(15, 5, 15, 5);
        boxOuter.addView(boxHeader);
        boxOuter.addView(boxInner);
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            switch (name) {
                case "header":
                    boxHeader.setPadding(15, 0, 15, 5);
                    TextView headerView = readHeader(parser);
                    headerView.setTextColor(boxHeaderTextColor);
                    headerView.setTextSize(14);
                    headerView.setGravity(Gravity.START);
                    boxHeader.addView(headerView);
                    break;
                case "text":
                    boxInner.addView(readTextTag(parser));
                    break;
                case "box":
                    LinearLayout box = readBox(parser);
                    boxInner.addView(box);
                    break;
                case "list":
                    boxInner.addView(readList(parser));
                    break;
                default:
                    skip(parser);
                    break;
            }
        }
        parser.require(XmlPullParser.END_TAG, ns, "box");

        LinearLayout.LayoutParams boxOuterParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        boxOuterParams.setMargins(10,10,10,10);
        boxOuter.setLayoutParams(boxOuterParams);
        boxOuter.setOrientation(LinearLayout.VERTICAL);
        boxOuter.setBackgroundColor(boxBorder);
        boxOuter.setPadding(10,10,10,10);

        return boxOuter;
    }

    private HorizontalScrollView readTable(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "table");
        TableLayout tableLayout = new TableLayout(this);
        int count = 0;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            if (parser.getName().equals("tr")) {
                TableRow tableRow = readTableRow(parser, count++);
                tableLayout.addView(tableRow);
            } else {
                skip(parser);
            }
        }
        parser.require(XmlPullParser.END_TAG, ns, "table");

        tableLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        tableLayout.setStretchAllColumns(true);

        HorizontalScrollView scrollView = new HorizontalScrollView(this);
        scrollView.addView(tableLayout);

        return scrollView;
    }

    private TableRow readTableRow(XmlPullParser parser, int count) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "tr");
        TableRow tableRow = new TableRow(this);
        TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                                                                    TableRow.LayoutParams.WRAP_CONTENT);
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            TextView textView = null;
            switch (name) {
                case "header":
                    textView = readHeader(parser);
                    textView.setTextSize(14);
                    textView.setBackgroundColor(tableHeaderBackground);
                    textView.setTextColor(tableHeaderTextColor);
                    break;
                case "text":
                    textView = readTextTag(parser);
                    break;
                default:
                    skip(parser);
                    break;
            }
            if (textView != null) {
                textView.setLayoutParams(rowParams);
                tableRow.addView(textView);
            }
        }
        parser.require(XmlPullParser.END_TAG, ns, "tr");

        TableLayout.LayoutParams tableParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT,
                                                                            TableLayout.LayoutParams.WRAP_CONTENT);
        tableRow.setLayoutParams(tableParams);
        if (count % 2 == 0) {
            tableRow.setBackgroundColor(tableAltBackground);
        }

        return tableRow;
    }

    private String listNumber(int listLevel, int count) {
        String number = "";
        switch(listLevel) {
            case 1:
                break;
            case 2:
                break;
            case 3:
                break;
            case 4:
                break;
            default:
                number = " " + Integer.toString(count) + " ";
                break;
        }
        return number;
    }

    private class NumberedListItem {
        private int mLevel;
        private String mValue;
        private String mTextAlign;
        public List<NumberedListItem> mNumberedList = new ArrayList<>();

        public NumberedListItem(int level, String value, String textAlign) {
            mLevel = level;
            mValue = value;
            mTextAlign = textAlign;
        }

        public int getLevel() {
            return mLevel;
        }
        public String getValue() {
            return mValue;
        }
        public String getTextAlign() {
            return mTextAlign;
        }
    }

    private String listBullet(int listLevel) {
        String bullet = " ";
        switch(listLevel) {
            case 1:
                bullet = "⁃";
                break;
            case 2:
                bullet = "◦";
                break;
            case 3:
                bullet = "‣";
                break;
            case 4:
                bullet = "◘";
                break;
            default: // also case 0
                bullet = "•";
                break;
        }
        bullet = bullet.concat(" ");
        return bullet;
    }
    private String orderedListBullet(int listLevel, int index) {
        String bullet = " ";
        switch(listLevel % 3) {
            case 1: // capital letters
                bullet = (char)(index%27 + 65) + ".";
                break;
            case 2: // lowercase letters
                bullet = (char)(index%27 + 97) + ".";
                break;
            default: // numbers (also case 0)
                bullet = index + ".";
                break;
        }
        bullet = bullet.concat(" ");
        return bullet;
    }

    private List<NumberedListItem> parseNumberedList(int index, int lastLevel, List<NumberedListItem> list) {
        List<NumberedListItem> fill = new ArrayList<>();
        // loop through the list
        while (index < list.size()) {
            // if the current item is one level higher
            if (lastLevel < list.get(index).getLevel()) {
                // call this function on the elements, and add them to the current item
                fill.get(fill.size()-1).mNumberedList.addAll(parseNumberedList(index, list.get(index).getLevel(), list));
                // catch the list up to where it should be
                while (index < list.size() && lastLevel < list.get(index).getLevel())
                    index++;
            } else if (lastLevel > list.get(index).getLevel()) { // if the current item is a level lower
                return fill; // it should go into the parent list
            } else { // otherwise continue adding items into the list
                fill.add(list.get(index));
                index++;
            }
        }
        return fill;
    }

    private void printNumberedList(List<NumberedListItem> list, int level) {
        for (int i = 0; i < list.size(); i++) {
            Log.d("OaTS"+level, i+ " " + list.get(i).getLevel() + " " + list.get(i).getValue());
            if (list.get(i).mNumberedList.size() > 0) {
                printNumberedList(list.get(i).mNumberedList, level+1);
            }
        }
    }

    private void placeOrderedList(List<NumberedListItem> list, int level, LinearLayout listLayout) {
        for (int i = 0; i < list.size(); i++) {
            SpannableStringBuilder text = new SpannableStringBuilder(orderedListBullet(level, i)).append(list.get(i).getValue());
            TextView textView = new TextView(this);
            textView.setText(text);
            textView.setGravity(textViewGravity(list.get(i).getTextAlign()));

            textView.setPadding(40 * (level) + 30, 0, 15, 0);
            listLayout.addView(textView);
            if (list.get(i).mNumberedList.size() > 0) {
                placeOrderedList(list.get(i).mNumberedList, level+1, listLayout);
            }
        }
    }

    private LinearLayout readList(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "list");
        LinearLayout listLayout = new LinearLayout(this);
        boolean ordered = parser.getAttributeValue(null, "type").equals("ordered");
        if (ordered) {
            List<NumberedListItem> numberedList = new ArrayList<>();
            List<NumberedListItem> intoList = numberedList;
            int lastLevel = 0;
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                String name = parser.getName();
                String textAlign = "";
                String text;
                int listLevel;
                switch (name) {
                    case "text":
                        parser.require(XmlPullParser.START_TAG, ns, "text");
                        textAlign = parser.getAttributeValue(null, "textAlign");
                        String listLevelStr = parser.getAttributeValue(null, "level");
                        listLevel = Integer.parseInt(listLevelStr == null ? "0" : listLevelStr);
                        text = readText(parser).toString();
                        parser.require(XmlPullParser.END_TAG, ns, "text");
                        if (lastLevel > listLevel) {
                            intoList = intoList.get(intoList.size()-1).mNumberedList;
                        }
                        intoList.add(new NumberedListItem(listLevel, text, textAlign));
                        break;
                    default:
                        skip(parser);
                        break;
                }
            }
            numberedList = parseNumberedList(0, 0, numberedList);
            printNumberedList(numberedList, 0);
            placeOrderedList(numberedList, 0, listLayout);
        } else {
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                String name = parser.getName();
                SpannableStringBuilder text = new SpannableStringBuilder("");
                String textAlign = "";
                boolean textSet = false;
                int listLevel = 0;
                switch (name) {
                    case "text":
                        parser.require(XmlPullParser.START_TAG, ns, "text");
                        textAlign = parser.getAttributeValue(null, "textAlign");
                        String listLevelStr = parser.getAttributeValue(null, "level");
                        listLevel = Integer.parseInt(listLevelStr == null ? "0" : listLevelStr);
                        text = new SpannableStringBuilder(listBullet(listLevel)).append(readText(parser));
                        parser.require(XmlPullParser.END_TAG, ns, "text");
                        textSet = true;
                        break;
                    default:
                        skip(parser);
                        break;
                }
                if (textSet) {
                    TextView textView = new TextView(this);
                    textView.setText(text);
                    textView.setGravity(textViewGravity(textAlign));

                    textView.setPadding(30 * (listLevel + 1), 0, 15, 0);
                    listLayout.addView(textView);
                }
            }
        }
        parser.require(XmlPullParser.END_TAG, ns, "list");

        LinearLayout.LayoutParams listParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        listLayout.setOrientation(LinearLayout.VERTICAL);
        listLayout.setLayoutParams(listParams);

        return listLayout;
    }

    private SpannableStringBuilder readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        SpannableStringBuilder result = new SpannableStringBuilder("");
        while (parser.next() != XmlPullParser.END_TAG) {
            switch(parser.getEventType()) {
                case XmlPullParser.TEXT:
                    result.append(parser.getText());
                    break;
                case XmlPullParser.START_TAG:
                    String name = parser.getName();
                    SpannableStringBuilder text = readText(parser);
                    switch(name) {
                        case "b":
                            text.setSpan(new StyleSpan(Typeface.BOLD),
                                        0, text.length(),
                                         Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            break;
                        case "i":
                            text.setSpan(new StyleSpan(Typeface.ITALIC),
                                        0, text.length(),
                                         Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            break;
                        case "bi":
                            text.setSpan(new StyleSpan(Typeface.BOLD_ITALIC),
                                        0, text.length(),
                                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            break;
                        default:
                            break;
                    }
                    result.append(text);
                    break;
                default:
                    skip(parser);
                    break;
            }
        }
        return result;
    }

    private void skip(XmlPullParser parser) throws IOException, XmlPullParserException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }
}