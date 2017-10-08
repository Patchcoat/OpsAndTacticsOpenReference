package c1.opsandtacticsopenreference;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Build;
import android.support.v7.widget.Toolbar;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Typeface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.Xml;
import android.text.SpannableStringBuilder;
import android.text.Spannable;
import android.preference.PreferenceManager;


import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class XMLActivity extends AppCompatActivity {

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
    private static final String ns = null;
    // Bookmarks
    String bookmarkCollection;
    String pageLink;
    String pageName;
    DBHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xml);

        // Bookmarks
        db = DBHandler.getInstance(getApplicationContext());
        db.closeDB();
    }

    @Override
    @SuppressWarnings("ResourceType")
    protected void onResume(){
        super.onResume();
        // setup Preferences
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(XMLActivity.this);
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
            DrawableCompat.setTintMode(wrapDrawable, Mode.SRC_IN);
            bar.setOverflowIcon(wrapDrawable);
        }
        // Set the action bar
        setSupportActionBar(bar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String page = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        String name = intent.getStringExtra(MainActivity.EXTRA_MESSAGE2);

        pageLink = page;
        pageName = name;
        SharedPreferences settings = getSharedPreferences("bookmarkCollection", 0);
        bookmarkCollection = settings.getString("bookmarkCollection", "default");

        // Change the background color of the most parent layout
        linearLayout = (LinearLayout) findViewById(R.id.TopLevelLayout);
        linearLayout.setBackgroundColor(background);

        // remove everything already in the view
        LinearLayout linLay = (LinearLayout) findViewById(R.id.parentLayout);
        linLay.removeAllViews();

        // Fill the page using the XML file pointed to
        try {
            parse(page);
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Skip a tag
    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
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

    // For the tags title and summary, extracts their text values.
    private String readTagText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    // Parses the contents of an item. If it encounters a header or text tag, hands them off
    // to their respective "read" methods for processing. Otherwise, skips the tag.
    private void readItem(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "item");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            String textAlign = parser.getAttributeValue(null, "textAlign");
            linearLayout = (LinearLayout) findViewById(R.id.parentLayout);
            if (name.equals("header")) {
                readHeader(parser, parser.getAttributeValue(null, "level"), null, parser.getAttributeValue(null, "textAlign"), linearLayout);
            } else if (name.equals("text")) {
                readText(parser, parser.getAttributeValue(null, "textAlign"), linearLayout, null, 0);
            } else if (name.equals("box")) {
                LinearLayout box = readBox(parser);
                linearLayout.addView(box);
            } else if (name.equals("list")){
                readList(parser, parser.getAttributeValue(null, "type"), linearLayout);
            } else if (name.equals("table")){
                boolean big = false;
                if (parser.getAttributeValue(null, "size") != null &&
                        parser.getAttributeValue(null, "size").equals("big")){
                    big = true;
                }
                readTable(parser, linearLayout, big);
            } else {
                skip(parser);
            }
        }
    }

    // Processes table tags
    private void readTable(XmlPullParser parser, LinearLayout container, boolean big) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "table");
        HorizontalScrollView scrollView = new HorizontalScrollView(this);
        // if the table is particularly large then set it to wrap content
        // otherwise sat it to expand fully
        scrollView.setLayoutParams(
                    new HorizontalScrollView.LayoutParams(
                            HorizontalScrollView.LayoutParams.MATCH_PARENT,
                            HorizontalScrollView.LayoutParams.MATCH_PARENT));
        container.addView(scrollView);

        LinearLayout localLinearLayout = new LinearLayout(this);
        LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        linearLayoutParams.setMargins(10,0,10,0);
        localLinearLayout.setOrientation(LinearLayout.VERTICAL);
        localLinearLayout.setLayoutParams(linearLayoutParams);
        if (big) {
            scrollView.addView(localLinearLayout);
        } else {
            container.addView(localLinearLayout);
        }

        TableLayout tableLayout = new TableLayout(this);
        TableLayout.LayoutParams tableLayoutParams= new TableLayout.LayoutParams(
                TableLayout.LayoutParams.WRAP_CONTENT,
                TableLayout.LayoutParams.WRAP_CONTENT);

        tableLayout.setLayoutParams(tableLayoutParams);
        tableLayout.setStretchAllColumns(true);
        int tableRowNum = 0;
        while (parser.next() != XmlPullParser.END_TAG){
            if (parser.getEventType() != XmlPullParser.START_TAG){
                continue;
            }
            String name = parser.getName();
            if (name.equals("tr")){
                if (tableRowNum == 0){
                    readTableRow(parser, tableLayout, tableRowNum);
                } else {
                    readTableRow(parser, tableLayout, tableRowNum);
                }
            } else {
                skip(parser);
            }
            tableRowNum++;
        }
        //use a GradientDrawable with only one color set, to make it a solid color
        GradientDrawable border = new GradientDrawable();
        border.setColor(background); //white background
        border.setStroke(1, tableBorder); //black border with full opacity
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            tableLayout.setBackgroundDrawable(border);
        } else {
            tableLayout.setBackground(border);
        }
        localLinearLayout.addView(tableLayout);
        parser.require(XmlPullParser.END_TAG, ns, "table");
    }

    // Processes tr tags
    private void readTableRow(XmlPullParser parser, TableLayout tableLayout, int tableRowNum) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "tr");
        TableLayout.LayoutParams tableParams =
                new TableLayout.LayoutParams(
                        TableLayout.LayoutParams.WRAP_CONTENT,
                        TableLayout.LayoutParams.WRAP_CONTENT);
        TableRow tableRow = new TableRow(this);
        tableRow.setLayoutParams(tableParams);
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            LinearLayout rowItem = new LinearLayout(this);
            if (name.equals("text")) {
                String textAlign = parser.getAttributeValue(null, "textAlign");
                readText(parser, textAlign, rowItem, null, 0);
                if (textAlign != null){
                    switch (textAlign){
                        case "center":
                            rowItem.setGravity(Gravity.CENTER);
                            break;
                        case "right":
                            rowItem.setGravity(Gravity.RIGHT);
                            break;
                        case "left":
                            rowItem.setGravity(Gravity.LEFT);
                            break;
                        default:
                            rowItem.setGravity(Gravity.RIGHT);
                    }
                }
            } else if (name.equals("header")) {
                String textAlign = parser.getAttributeValue(null, "textAlign");
                readHeader(parser, "13", textAlign, "#"+Integer.toHexString(altText), rowItem);
                if (textAlign != null){
                    switch (textAlign){
                        case "center":
                            rowItem.setGravity(Gravity.CENTER);
                            break;
                        case "right":
                            rowItem.setGravity(Gravity.RIGHT);
                            break;
                        case "left":
                            rowItem.setGravity(Gravity.LEFT);
                            break;
                        default:
                            rowItem.setGravity(Gravity.RIGHT);
                    }
                }
            } else {
                skip(parser);
            }
            //use a GradientDrawable with only one color set, to make it a solid color
            GradientDrawable border = new GradientDrawable();
            if (tableRowNum == 0){
                border.setColor(tableBorder);
            } else if (tableRowNum % 2 == 0){
                border.setColor(altBackground);
            } else {
                border.setColor(background);
            }
            border.setStroke(1, tableBorder); //black border with full opacity
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                rowItem.setBackgroundDrawable(border);
            } else {
                rowItem.setBackground(border);
            }
            tableRow.addView(rowItem);
        }
        tableLayout.addView(tableRow);
        parser.require(XmlPullParser.END_TAG, ns, "tr");
    }

    // Processes list tags
    private void readList(XmlPullParser parser, String type, LinearLayout container) throws IOException, XmlPullParserException{
        parser.require(XmlPullParser.START_TAG, ns, "list");
        ArrayList<Integer> listIndex = new ArrayList<>();//Four levels to the list ought to be enough for anyone
        int listLevel = 0;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            String bullet = parser.getAttributeValue(null, "bullet");
            if (name.equals("text")) {
                // get the indentation level
                listLevel = 0;
                String listLevelString = parser.getAttributeValue(null, "level");
                if (listLevelString != null) {
                    listLevel = Integer.valueOf(listLevelString);
                }
                // If there isn't an index yet for this level of list create an index
                if (listIndex.size() < listLevel+1){
                    while(listIndex.size() < listLevel+1){
                        listIndex.add(1);
                    }
                }
                if (type == null || (type != null && type.equals("unordered"))) {
                    // if no bullet is specified
                    if (bullet == null){
                        // bullet levels
                        if (listLevel == 0){
                            bullet = "•";
                        } else if (listLevel == 1){
                            bullet = "–";
                        } else {
                            bullet = "◦";
                        }// if you have more levels than this you have a problem
                    }
                    readText(parser, parser.getAttributeValue(null, "textAlign"), container, bullet, listLevel);
                } else if (type != null && type.equals("ordered")){
                    if (bullet == null){
                        // if the level is even
                        if ((listLevel & 1) == 0){
                            bullet = String.valueOf(listIndex.get(listLevel)) + ".";
                            // use numbers
                        } else {
                            bullet = ((char) ('a' - 1 + listIndex.get(listLevel))) + ".";
                            // otherwise use letters
                        }
                    }
                    readText(parser, parser.getAttributeValue(null, "textAlign"), container, bullet, listLevel);
                    listIndex.set(listLevel, listIndex.get(listLevel)+1);
                }
            } else {
                skip(parser);
            }
        }
        parser.require(XmlPullParser.END_TAG, ns, "list");
    }

    // Processes box tags
    private LinearLayout readBox (XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "box");
        LinearLayout boxOuter = new LinearLayout(this);
        LinearLayout boxInner = null;
        LinearLayout boxHeader = null;
        LinearLayout.LayoutParams boxOuterParams = new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        boxOuterParams.setMargins(10,10,10,10);
        boxOuter.setLayoutParams(boxOuterParams);
        boxOuter.setOrientation(LinearLayout.VERTICAL);
        boxOuter.setBackgroundColor(boxBorder);
        boxOuter.setPadding(4,4,4,4);
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("header")) {
                boxHeader = new LinearLayout(this);
                LinearLayout.LayoutParams boxHeaderParams = new LayoutParams(
                        LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
                boxHeaderParams.setMargins(0,0,0,0);
                boxHeader.setLayoutParams(boxHeaderParams);
                boxHeader.setOrientation(LinearLayout.VERTICAL);
                boxHeader.setBackgroundColor(0x00000000);
                boxHeader.setPadding(15,3,15,5);
                boxOuter.addView(boxHeader);
                readHeader(parser, "13", parser.getAttributeValue(null, "textAlign"), "#"+Integer.toHexString(altText), boxHeader);//TODO instead of "13" reference settings
            } else if (name.equals("text")) {
                boxInner = new LinearLayout(this);
                LinearLayout.LayoutParams boxInnerParams = new LayoutParams(
                        LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
                boxInnerParams.setMargins(0,0,0,0);
                boxInner.setLayoutParams(boxInnerParams);
                boxInner.setOrientation(LinearLayout.VERTICAL);
                boxInner.setBackgroundColor(altBackground);
                boxInner.setPadding(15,5,15,5);
                boxOuter.addView(boxInner);
                readText(parser, parser.getAttributeValue(null, "textAlign"), boxInner, null, 0);
            } else if(name.equals("box")){
                LinearLayout box = readBox(parser);
                if (boxInner != null){
                    boxInner.addView(box);
                } else {
                    boxOuter.addView(box);
                }
            } else if(name.equals("list")){
                if (boxInner == null){
                    boxInner = new LinearLayout(this);
                    LinearLayout.LayoutParams boxInnerParams = new LayoutParams(
                            LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
                    boxInnerParams.setMargins(0,0,0,0);
                    boxInner.setLayoutParams(boxInnerParams);
                    boxInner.setOrientation(LinearLayout.VERTICAL);
                    boxInner.setBackgroundColor(altBackground);
                    boxInner.setPadding(15,5,15,5);
                    boxOuter.addView(boxInner);
                }
                readList(parser, parser.getAttributeValue(null, "type"), boxInner);
            } else {
                skip(parser);
            }
        }
        return boxOuter;
    }

    // Processes header tags
    private void readHeader(XmlPullParser parser, String level, String textAlign, String colorString, LinearLayout container) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "header");
        if (colorString == null){
            colorString = parser.getAttributeValue(null, "color");
        }
        String header = readTagText(parser);
        int color = textColor;
        if (colorString != null && !colorString.isEmpty() &&
                colorString.matches("^#{1}([0-9a-fA-F]{6}|[0-9a-fA-F]{8})$")){
            color = Color.parseColor(colorString);
        }
        parser.require(XmlPullParser.END_TAG, ns, "header");
        TextView textView = new TextView(this);
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
        textView.setText(header);
        textView.setTextSize(textSize);
        textView.setPadding(15, 0,15,0);
        textView.setTextColor(color);
        textView.setTypeface(null, Typeface.BOLD);
        if (textAlign != null){
            switch (textAlign){
                case "center":
                    textView.setGravity(Gravity.CENTER);
                    break;
                case "right":
                    textView.setGravity(Gravity.RIGHT);
                    break;
                case "left":
                    textView.setGravity(Gravity.LEFT);
                    break;
                default:
                    textView.setGravity(Gravity.RIGHT);
            }
        }
        Typeface font = Typeface.createFromAsset(
                getAssets(),
                "font/"+bodyFont);
        textView.setTypeface(font);
        container.addView(textView);
    }

    // Processes text tags
    private void readText(XmlPullParser parser, String textAlign, LinearLayout container, String listPrefix, int level) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "text");
        int eventType = parser.getEventType();
        SpannableStringBuilder text = new SpannableStringBuilder("");
        SpannableStringBuilder tempText = new SpannableStringBuilder("");
        int color = textColor;
        String fontFormat = bodyFont;
        while (parser.next() != XmlPullParser.END_TAG) {
            tempText.clear();
            eventType = parser.getEventType();
            if (eventType == XmlPullParser.TEXT) {
                tempText.replace(0, tempText.length(), parser.getText());
                Typeface font = Typeface.createFromAsset(
                        getAssets(),
                        "font/"+bodyFont);
                tempText.setSpan(
                        new CustomTypefaceSpan("", font),
                        0,
                        tempText.length(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                text.append(tempText);
                continue;
            } else if (eventType != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            String colorString = parser.getAttributeValue(null, "color");
            int localColor = color;
            // Applies style to the tagged places
            // This whole section is one big, dirty hack.
            if (name.equals("b")) {
                tempText.replace( 0, tempText.length(), readStyle(parser, "b"));
                fontFormat = new StringBuffer(fontFormat).insert(
                                fontFormat.length()-4, "_bold").toString();
                Typeface font = Typeface.createFromAsset(
                        getAssets(),
                        "font/"+fontFormat);
                tempText.setSpan(
                        new CustomTypefaceSpan("", font),
                        0,
                        tempText.length(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (name.equals("i")){
                tempText.replace(0, tempText.length(), readStyle(parser, "i"));
                fontFormat = new StringBuffer(fontFormat).insert(
                        fontFormat.length()-4, "_italic").toString();
                Typeface font = Typeface.createFromAsset(
                        getAssets(),
                        "font/"+bodyFont);
                tempText.setSpan(
                        new CustomTypefaceSpan("", font),
                        0,
                        tempText.length(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (name.equals("bi")){
                tempText.replace(0,tempText.length(),readStyle(parser, "bi"));
                fontFormat = new StringBuffer(fontFormat).insert(
                        fontFormat.length()-4, "_bolditalic").toString();
                Typeface font = Typeface.createFromAsset(
                        getAssets(),
                        "font/"+bodyFont);
                tempText.setSpan(
                        new CustomTypefaceSpan("", font),
                        0,
                        tempText.length(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//                tempText.setSpan(
//                        new android.text.style.StyleSpan(android.graphics.Typeface.BOLD_ITALIC),
//                        0,
//                        tempText.length(),
//                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (name.equals("font")){
                String fontName = parser.getAttributeValue(null, "fontName");
                tempText.replace(0,tempText.length(),readStyle(parser, "font"));
                if (fontName != null && fontName.equals("secondary")){
                    Typeface font = Typeface.createFromAsset(
                            getAssets(),
                            "font/"+secondaryFont);
                    tempText.setSpan(
                            new CustomTypefaceSpan("", font),
                            0,
                            tempText.length(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                else if (fontName != null){
                    Typeface font = Typeface.createFromAsset(
                            getAssets(),
                            "font/"+fontName);
                    tempText.setSpan(
                            new CustomTypefaceSpan("", font),
                            0,
                            tempText.length(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            } else {
                skip(parser);
            }
            if (colorString != null && !colorString.isEmpty() &&
                    colorString.matches("^#{1}([0-9a-fA-F]{6}|[0-9a-fA-F]{8})$")){
                localColor = Color.parseColor(colorString);
            } else {
                localColor = color;
            }
            tempText.setSpan(
                    new ForegroundColorSpan(localColor),
                    0,
                    tempText.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            text.append(tempText);
        }
        if (listPrefix != null && listPrefix != "") {
            text.setSpan(new ListIndentSpan(15 + level * 15, 15, listPrefix), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        parser.require(XmlPullParser.END_TAG, ns, "text");
        TextView textView = new TextView(this);
        textView.setText(text, TextView.BufferType.SPANNABLE);
        textView.setTextSize(bodyTextSize);
        textView.setPadding(15,0,15,0); // Left, Top, Right, Bottom
        textView.setTextColor(color);
        if (textAlign != null){
            switch (textAlign){
                case "center":
                    textView.setGravity(Gravity.CENTER);
                    break;
                case "right":
                    textView.setGravity(Gravity.RIGHT);
                    break;
                case "left":
                    textView.setGravity(Gravity.LEFT);
                    break;
                default:
                    textView.setGravity(Gravity.RIGHT);
            }
        }
        container.addView(textView);
    }

    // Process the text style
    private String readStyle(XmlPullParser parser, String tag) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, tag);
        String text = readTagText(parser);
        parser.require(XmlPullParser.END_TAG, ns, tag);
        return text;
    }

    private void readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "body");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals("item")) {
                readItem(parser);
            } else {
                skip(parser);
            }
        }
    }

    public void parse(String file) throws XmlPullParserException, IOException {
        InputStream in = getAssets().open(file);
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            readFeed(parser);
        } finally {
            in.close();
        }
    }

    public void createBookmark(){
        Log.i("Bookmark Collection", bookmarkCollection);
        Log.i("Bookmark Link", pageLink);
        Log.i("Bookmark Name", pageName);

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

        Bookmark bookmark = new Bookmark(pageName, pageLink, "xml");
        long bookmark_id = db.addBookmark(bookmark, new long[]{collection_id});
    }

    public void selectCollection(int collection_id){
        db = DBHandler.getInstance(getApplicationContext());
        String collection = db.getCollection(collection_id).getCollection();
        SharedPreferences settings = getSharedPreferences("bookmarkCollection", 0);
        SharedPreferences.Editor settings_editor = settings.edit();
        settings_editor.putString("bookmarkCollection", collection);
        settings_editor.apply();
        bookmarkCollection = collection;
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

}
