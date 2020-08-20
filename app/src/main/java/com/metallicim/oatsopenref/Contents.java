package com.metallicim.oatsopenref;

import android.content.res.Resources;

import java.util.List;

public class Contents implements ParentListItem {
    private String mName;
    private List<Category> mCategories;
    private Resources.Theme mTheme;

    public Contents(String name, List<Category> categories) {
        mName = name;
        mCategories = categories;
    }

    public String getName() {
        return mName;
    }

    public void setTheme(Resources.Theme theme) {
        mTheme = theme;
    }
    public Resources.Theme getTheme() {
        return mTheme;
    }

    @Override
    public List<?> getChildItemList() {
        return mCategories;
    }
    public boolean isInitiallyExpanded() {
        return false;
    }
}
