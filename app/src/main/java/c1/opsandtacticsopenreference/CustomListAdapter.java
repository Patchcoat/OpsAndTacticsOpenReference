package c1.opsandtacticsopenreference;

/**
 * Created by c1user on 10/2/17.
 */

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CustomListAdapter extends ArrayAdapter<TextAssetLink>{

    private Context _context;

    // Theme
    private Typeface _font;
    private int _textColor;
    private int _background;

    public CustomListAdapter(Context context, ArrayList<TextAssetLink> data,
                             Typeface font, int textColor, int background) {
        super(context, R.layout.list_group, data);
        this._context=context;
        this._font = font;
        this._textColor = textColor;
        this._background = background;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        String headerTitle = getItem(position).Text();

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_group, null);
        }

        TextView lblListHeader = (TextView) convertView.findViewById(R.id.lblListHeader);
        lblListHeader.setTypeface(_font);
        lblListHeader.setTextColor(_textColor);
        lblListHeader.setText(headerTitle);
        LinearLayout lblListHeaderLayout = (LinearLayout) convertView
                .findViewById(R.id.lblListHeaderLayout);
        lblListHeaderLayout.setBackgroundColor(_background);

        return convertView;
    }
}
