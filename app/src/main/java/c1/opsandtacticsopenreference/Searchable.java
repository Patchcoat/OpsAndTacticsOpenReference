package c1.opsandtacticsopenreference;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class Searchable extends ListActivity {

    public static final String EXTRA_MESSAGE = "c1.opsandtacticsopenrefernece.MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchable);

        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Search(query);
        }
    }

    // preform the search
    private void Search(String query){
        Log.i("Search", query);

    }
}
