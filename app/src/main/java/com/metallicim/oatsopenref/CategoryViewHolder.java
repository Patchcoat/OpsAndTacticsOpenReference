package com.metallicim.oatsopenref;

import android.content.res.Resources;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

public class CategoryViewHolder extends ChildViewHolder {
    private TextView mCategoryTextView;
    private Category mCategory;

    public CategoryViewHolder(View itemView) {
        super(itemView);
        mCategoryTextView = (TextView) itemView.findViewById(R.id.category_text);
    }

    public void bind(Category categories) {
        mCategory = categories;
        mCategoryTextView.setText(categories.getName());
        Resources.Theme theme = categories.getTheme();
        TypedValue color = new TypedValue();
        theme.resolveAttribute(android.R.attr.textColor, color, true);
        mCategoryTextView.setTextColor(color.data);
    }

    @Override
    public void gotoSubPage(View v) {
        Log.d("OaTS_", mCategory.getName());
        mCategory.gotoSubPage();
    }
}
