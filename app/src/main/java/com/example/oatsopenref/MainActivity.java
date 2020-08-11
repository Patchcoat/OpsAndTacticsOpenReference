package com.example.oatsopenref;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_MESSAGE = "com.example.oatsopenref.MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // setup toolbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        // load contents
        JSONArray contents = new JSONArray();
        try {
            contents = loadJSONFromAsset(getApplicationContext());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // fill recycler view
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        List<Contents> contentsList = new ArrayList<>();
        try {
            for (int i = 1; i < contents.length(); i++) {
                JSONObject contentsObj = contents.getJSONObject(i);
                List<Category> categories = new ArrayList<>();
                for (int j = 0; j < contentsObj.getJSONArray("children").length(); j++) {
                    JSONObject categoryObj = contentsObj.getJSONArray("children").getJSONObject(j);
                    Category category = new Category(this,
                                                     categoryObj.getString("text"),
                                                     categoryObj.getString("link"),
                                                     categoryObj.getString("type"));
                    categories.add(category);
                }
                Contents content = new Contents(contentsObj.getString("text"), categories);
                contentsList.add(content);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final List<Contents> contentsListFinal = contentsList;
        ContentsAdapter mAdapter = new ContentsAdapter(getApplicationContext(), contentsListFinal);
        for (int i = 0; i < contentsListFinal.size(); i++) {
            Contents content = contentsListFinal.get(i);
            for (int j = 0; j < content.getChildItemList().size(); j++) {
                Category cat = (Category) content.getChildItemList().get(j);
            }
        }
        mAdapter.setExpandCollapseListener(new ExpandableRecyclerAdapter.ExpandCollapseListener() {
            @Override
            public void onListItemExpanded(int position) {
                Contents expandedContents = contentsListFinal.get(position);
            }

            @Override
            public void onListItemCollapsed(int position) {
                Contents collapsedContents = contentsListFinal.get(position);
            }
        });
        recyclerView.setAdapter(mAdapter);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
    }

    public JSONArray loadJSONFromAsset(Context context) throws JSONException {
        String json;
        try {
            InputStream is = context.getAssets().open("Contents.json");
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return new JSONArray(json);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
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
}