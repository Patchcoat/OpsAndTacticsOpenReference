package com.example.oatsopenref;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.util.Xml;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.LinearLayout;
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

    String pageLink;
    private static final String ns = null;
    LinearLayout container;

    int headerTextSize = 40;
    int boxBorder = 0xff000000;
    int altBackground = 0xffffffff;
    int boxBackground = 0xff000000;
    int altText = 0xff000000;

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
                    TextView headerView = readHeader(parser);
                    container.addView(headerView);
                    break;
                case "text":
                    TextView textView = readTextTag(parser);
                    container.addView(textView);
                    break;
                case "box":
                    LinearLayout boxLayout = readBox(parser);
                    container.addView(boxLayout);
                    break;
                case "table":
                    LinearLayout tableLayout = readTable(parser);
                    container.addView(tableLayout);
                    break;
                default:
                    skip(parser);
                    break;
            }
        }
    }

    private TextView readHeader(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "header");
        String level = parser.getAttributeValue(null, "level");
        String header = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "header");

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

        TextView textView = new TextView(this);
        textView.setText(header);
        textView.setTextSize(textSize);
        textView.setPadding(15, 0,15,0);
        textView.setTypeface(null, Typeface.BOLD);
        textView.setGravity(Gravity.END);

        return textView;
    }

    private TextView readTextTag(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "text");
        String text = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "text");

        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setPadding(15,0,15,0);

        return textView;
    }

    private LinearLayout readBox(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "box");
        LinearLayout boxOuter = new LinearLayout(this);
        LinearLayout boxInner = null;
        LinearLayout boxHeader;
        LinearLayout.LayoutParams boxOuterParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        boxOuterParams.setMargins(10,10,10,10);
        boxOuter.setLayoutParams(boxOuterParams);
        boxOuter.setOrientation(LinearLayout.VERTICAL);
        boxOuter.setBackgroundColor(boxBorder);
        boxOuter.setPadding(10,10,10,10);

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            switch (name) {
                case "header":
                    boxHeader = new LinearLayout(this);
                    LinearLayout.LayoutParams boxHeaderParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                    boxHeaderParams.setMargins(0, 0, 0, 0);
                    boxHeader.setLayoutParams(boxHeaderParams);
                    boxHeader.setOrientation(LinearLayout.VERTICAL);
                    boxHeader.setBackgroundColor(boxBackground);
                    boxHeader.setPadding(15, 3, 15, 5);
                    boxOuter.addView(boxHeader);
                    TextView headerView = readHeader(parser);
                    boxHeader.addView(headerView);
                    break;
                case "text":
                    boxInner = new LinearLayout(this);
                    LinearLayout.LayoutParams boxInnerParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                    boxInnerParams.setMargins(0, 0, 0, 0);
                    boxInner.setLayoutParams(boxInnerParams);
                    boxInner.setOrientation(LinearLayout.VERTICAL);
                    boxInner.setBackgroundColor(altBackground);
                    boxInner.setPadding(15, 5, 15, 5);
                    boxOuter.addView(boxInner);
                    TextView textView = readTextTag(parser);
                    boxInner.addView(textView);
                    break;
                case "box":
                    LinearLayout box = readBox(parser);
                    if (boxInner != null) {
                        boxInner.addView(box);
                    } else {
                        boxOuter.addView(box);
                    }
                    break;
                case "list":
                    break;
                default:
                    skip(parser);
                    break;
            }
        }

        parser.require(XmlPullParser.END_TAG, ns, "box");

        return boxOuter;
    }

    private LinearLayout readTable(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "table");
        LinearLayout tableLayout = new LinearLayout(this);

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            if (parser.getName().equals("tr")) {
                TableRow tableRow = readTableRow(parser);
                tableLayout.addView(tableRow);
            } else {
                skip(parser);
            }
        }

        parser.require(XmlPullParser.END_TAG, ns, "table");

        return tableLayout;
    }

    private TableRow readTableRow(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "tr");
        TableRow tableRow = new TableRow(this);
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            switch (name) {
                case "header":
                    break;
                case "text":
                    break;
                default:
                    skip(parser);
                    break;
            }
        }
        parser.require(XmlPullParser.END_TAG, ns, "tr");

        return tableRow;
    }

    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
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