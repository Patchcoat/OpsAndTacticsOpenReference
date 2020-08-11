package com.example.oatsopenref;

import android.util.Log;
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
    }

    @Override
    public void gotoSubPage(View v) {
        Log.d("OaTS_", mCategory.getName());
        mCategory.gotoSubPage();
    }
}
