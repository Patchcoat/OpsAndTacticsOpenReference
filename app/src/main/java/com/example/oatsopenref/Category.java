package com.example.oatsopenref;

import android.content.Context;
import android.content.Intent;

public class Category {
    private String mName;
    private String mLink;
    private PageType mType;
    private Context mContext;
    public static final String EXTRA_MESSAGE = "com.example.oatsopenref.MESSAGE";

    public Category(Context parentContext, String name, String link, String type) {
        mContext = parentContext;
        mName = name;
        mLink = link;
        if (type.equals("xml")) {
            mType = PageType.XML;
        } else if (type.equals("feat")) {
            mType = PageType.feat;
        } else if (type.equals("add_collection")) {
            mType = PageType.add_collection;
        } else if (type.equals("remove_collection")) {
            mType = PageType.remove_collection;
        } else {
            mType = PageType.error;
        }
    }

    public String getName() {
        return mName;
    }

    public void gotoSubPage() {
        Intent intent = new Intent(mContext, XMLActivity.class);
        String message = mLink;
        intent.putExtra(EXTRA_MESSAGE, message);
        mContext.startActivity(intent);
    }
}
