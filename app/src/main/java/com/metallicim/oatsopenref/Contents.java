package com.metallicim.oatsopenref;

import java.util.List;

public class Contents implements ParentListItem {
    private String mName;
    private List<Category> mCategories;

    public Contents(String name, List<Category> categories) {
        mName = name;
        mCategories = categories;
    }

    public String getName() {
        return mName;
    }

    @Override
    public List<?> getChildItemList() {
        return mCategories;
    }
    public boolean isInitiallyExpanded() {
        return false;
    }
}
