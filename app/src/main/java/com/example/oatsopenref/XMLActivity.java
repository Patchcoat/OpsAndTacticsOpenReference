package com.example.oatsopenref;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.util.Xml;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class XMLActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "com.example.oatsopenref.MESSAGE";

    String pageLink;
    private static final String ns = null;
    LinearLayout container;

    int headerTextSize = 40;
    int boxBorder = 0xff000000;
    int altBackground = 0xffffffff;
    int boxBackground = 0xff000000;
    int altText = 0xff000000;
    int tableAltBackground = 0xffe2e2e2;
    int tableHeaderBackground = 0xff000000;
    int tableHeaderText = 0xffffffff;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xml);

        // Setup message from start intent
        Intent intent = getIntent();
        pageLink = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);

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
        SharedPreferences settings = getSharedPreferences("bookmarkCollection", 0);

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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.content_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                finish();
                return super.onOptionsItemSelected(item);
            case R.id.action_about:
                Log.d("OaTS", "About");
                Intent intent = new Intent(this, XMLActivity.class);
                String message = "About.xml";
                intent.putExtra(EXTRA_MESSAGE, message);
                startActivity(intent);
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
        LinearLayout boxHeader = newBox(this, boxBackground);
        LinearLayout boxInner = newBox(this, altBackground);
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
                    headerView.setTextColor(0xffffffff);
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
                    textView.setTextColor(tableHeaderText);
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

    private LinearLayout readList(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "list");
        LinearLayout listLayout = new LinearLayout(this);
        boolean ordered = parser.getAttributeValue(null, "type").equals("ordered");
        int count = 0;
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
                    if (!ordered)
                        text = new SpannableStringBuilder(listBullet(listLevel)).append(readText(parser));
                    else // TODO ordered layout
                        text.append(" ");
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

                textView.setPadding(15 * (listLevel + 1),0,15,0);
                listLayout.addView(textView);
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