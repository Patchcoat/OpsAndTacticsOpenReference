package com.example.oatsopenref;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public class ContentsAdapter extends ExpandableRecyclerAdapter<ContentsViewHolder, CategoryViewHolder> {
    private LayoutInflater mInflator;

    public ContentsAdapter(Context context, List<? extends ParentListItem> parentItemList) {
        super(parentItemList);
        mInflator = LayoutInflater.from(context);
    }

    @Override
    public ContentsViewHolder onCreateParentViewHolder(ViewGroup parentViewGroup) {
        View contentsView = mInflator.inflate(R.layout.contents_view, parentViewGroup, false);
        return new ContentsViewHolder(contentsView);
    }

    @Override
    public CategoryViewHolder onCreateChildViewHolder(ViewGroup childViewGroup) {
        View categoryView = mInflator.inflate(R.layout.category_view, childViewGroup, false);
        return new CategoryViewHolder(categoryView);
    }

    @Override
    public void onBindParentViewHolder(ContentsViewHolder contentsViewHolder, int position, ParentListItem parentListItem) {
        Contents contents = (Contents) parentListItem;
        contentsViewHolder.bind(contents);
    }

    @Override
    public void onBindChildViewHolder(CategoryViewHolder categoryViewHolder, int position, Object childListItem) {
        Category category = (Category) childListItem;
        categoryViewHolder.bind(category);
    }
}
