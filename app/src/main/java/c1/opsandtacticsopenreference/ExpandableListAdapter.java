package c1.opsandtacticsopenreference;

import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
import android.widget.LinearLayout;

import c1.opsandtacticsopenreference.R;

public class ExpandableListAdapter extends BaseExpandableListAdapter {

    private Context _context;
    private List<TextAssetLink> _listDataHeader; // header titles
    // child data in format of header title, child title
    private HashMap<TextAssetLink, List<TextAssetLink>> _listDataChild;

    // Theme
    private Typeface _font;
    private Typeface _boldFont;
    private int _textColor;
    private int _altText;
    private int _background;
    private int _altBackground;

    public ExpandableListAdapter(Context context, List<TextAssetLink> listDataHeader,
                                 HashMap<TextAssetLink, List<TextAssetLink>> listChildData,
                                 Typeface font, Typeface boldFont, int textColor,
                                 int altText, int background, int altBackground) {
        this._context = context;
        this._listDataHeader = listDataHeader;
        this._listDataChild = listChildData;
        this._font = font;
        this._boldFont = boldFont;
        this._textColor = textColor;
        this._altText = altText;
        this._background = background;
        this._altBackground = altBackground;
    }

    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        return this._listDataChild.get(this._listDataHeader.get(groupPosition))
                .get(childPosititon);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        final String childText = ((TextAssetLink) getChild(groupPosition, childPosition)).Text();

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_item, null);
        }

        TextView txtListChild = (TextView) convertView
                .findViewById(R.id.lblListItem);
        txtListChild.setTypeface(_font);
        txtListChild.setBackgroundColor(_altBackground);
        txtListChild.setTextColor(_textColor);
        txtListChild.setText(childText);
        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this._listDataChild.get(this._listDataHeader.get(groupPosition))
                .size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this._listDataHeader.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this._listDataHeader.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        String headerTitle = ((TextAssetLink) getGroup(groupPosition)).Text();
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_group, null);
        }
        TextView lblListHeader = (TextView) convertView
                .findViewById(R.id.lblListHeader);
        lblListHeader.setTypeface(_boldFont);
        lblListHeader.setTextColor(_textColor);
        lblListHeader.setText(headerTitle);
        LinearLayout lblListHeaderLayout = (LinearLayout) convertView
                .findViewById(R.id.lblListHeaderLayout);
        lblListHeaderLayout.setBackgroundColor(_background);

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}