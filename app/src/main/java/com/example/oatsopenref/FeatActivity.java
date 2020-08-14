package com.example.oatsopenref;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Process;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.LinearLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class FeatActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "com.example.oatsopenref.MESSAGE";

    String pageLink;
    LinearLayout container;
    JSONArray contents;
    JSONArray feats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feat);

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
        } catch (JSONException e) {
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

    // binary search through the feats
    private int getFeatIndex(JSONArray array) throws JSONException {
        int start = 0;
        int end = array.length() - 1;
        int foundIndex = 0;
        while (start <= end) {
            int mid = (start + end) >> 1;// add L and R then divide by 2
            JSONObject obj = (JSONObject) array.get(mid);
            String str = obj.getString("category");
            if (str.compareToIgnoreCase(pageLink) < 0) {
                start = mid + 1;
            } else if (str.compareToIgnoreCase(pageLink) > 0) {
                end = mid - 1;
            } else {
                foundIndex = mid;
                break;
            }
        }
        return foundIndex;
    }

    private void loadFeatsInCategory() throws JSONException {
        int foundIndex = getFeatIndex(contents);
        feats = new JSONArray();
        // loop down through the JSONArray
        int downIndex = foundIndex;
        JSONObject nextObj;
        do {
            JSONObject obj = (JSONObject) contents.get(downIndex);
            feats.put(obj);
            if (downIndex <= 0)
                break;
            nextObj = (JSONObject) contents.get(--downIndex);
        } while(nextObj.getString("category").equals(pageLink));
        // reverse the array
        JSONArray featsTemp = new JSONArray();
        for (int i = feats.length() - 1; i >= 0; i--) {
            featsTemp.put(feats.get(i));
        }
        feats = featsTemp;
        // loop up through the JSONArray
        int upIndex = foundIndex;
        nextObj = (JSONObject) contents.get(++upIndex);
        while(nextObj.getString("category").equals(pageLink)) {
            JSONObject obj = (JSONObject) contents.get(upIndex);
            feats.put(obj);
            if (upIndex >= contents.length()-1)
                break;
            nextObj = (JSONObject) contents.get(++upIndex);
        }
    }

    private void parse(String pageLink) throws JSONException, IOException {
        loadJSONFromAsset(getApplicationContext());
        loadFeatsInCategory();

        displayFeats();
    }

    public void displayFeats() throws JSONException {
        for (int i = 0; i < feats.length(); i++) {
            JSONObject obj = (JSONObject) feats.get(i);
            Log.d("OaTS", obj.getString("category") + ": " + obj.getString("name"));
            String category = obj.getString("category");
            String name = obj.getString("name");
            String intro = obj.getString("intro");
            JSONArray prerequisiteJSONArray = obj.getJSONArray("prerequisite");
            List<String> prerequisiteList = new ArrayList<>();
            for (int j = 0; j < prerequisiteJSONArray.length(); j++)
                prerequisiteList.add((String) prerequisiteJSONArray.get(j));
            String benefit = obj.getString("benefit");
            String example = obj.getString("example");
            String normal = obj.getString("normal");
            String special = obj.getString("special");
            JSONObject tutoringJSONObject = obj.getJSONObject("tutoring");
            String tutoringCategories = tutoringJSONObject.getString("categories");
            String tutoringTime = tutoringJSONObject.getString("time");
            String tutoringBenefit = tutoringJSONObject.getString("benefit");

            
            //container.addView();
        }


    }

    public void loadJSONFromAsset(Context context) throws JSONException, IOException {
        String json;
        InputStream is = context.getAssets().open("Feats/feats.json");
        byte[] buffer = new byte[is.available()];
        is.read(buffer);
        is.close();
        json = new String(buffer, "UTF-8");
        contents = new JSONArray(json);
    }
}