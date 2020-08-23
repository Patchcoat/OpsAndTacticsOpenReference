package com.metallicim.oatsopenref;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class Category {
    private String mName;
    private String mLink;
    private PageType mType;
    private Context mContext;
    private Resources.Theme mTheme;
    private ContentsAdapter mContentsAdapter;
    public static final String EXTRA_MESSAGE = "com.metallicim.oatsopenref.MESSAGE";
    public static final String EXTRA_MESSAGE_NAME = "com.metallicim.oatsopenref.MESSAGE_NAME";

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
        } else if (type.equals("bookmark_collection")) {
            mType = PageType.bookmark_collection;
        } else {
            mType = PageType.error;
        }
    }

    // set Expandable Recycler Adapter
    public void setEra(ContentsAdapter era) {
        mContentsAdapter = era;
    }

    public String getName() {
        return mName;
    }
    public String getLink() {
        return mLink;
    }

    public void setTheme(Resources.Theme theme) {
        mTheme = theme;
    }
    public Resources.Theme getTheme() {
        return mTheme;
    }

    public Dialog addCollection() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);

        builder.setTitle(R.string.create_a_new_collection);

        View collectionView = inflater.inflate(R.layout.create_collection_popup, null);
        final EditText editText = collectionView.findViewById(R.id.editTextCollectionName);
        builder.setView(collectionView);

        final ContentsAdapter contentsAdapter = mContentsAdapter;
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                // user clicked ok
                String newCollection = editText.getText().toString();
                Bookmarks bookmarks = Bookmarks.getInstance();
                boolean collectionAdded = bookmarks.addCollection(newCollection);
                if (!collectionAdded) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle(R.string.collection_creation_error);
                    builder.setMessage(R.string.collection_already_exists);
                    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // user clicked ok
                        }
                    });
                    builder.show();
                } else {
                    List<? extends ParentListItem> parentList = contentsAdapter.getParentItemList();
                    Category bookmarkCategory =
                            new Category(mContext, newCollection,
                                    newCollection.toLowerCase().replace(" ", "_"),
                                    "bookmark_collection");
                    List<Category> list = (List<Category>) parentList.get(0).getChildItemList();
                    list.add(bookmarkCategory);
                    //contentsAdapter.getListItem();
                    contentsAdapter.notifyChildItemInserted(0, list.size()-1);
                    contentsAdapter.notifyChildItemChanged(0, list.size()-1);
                    bookmarks.updateFile(mContext);
                }
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // user clicked cancel
            }
        });

        return builder.create();
    }

    public Dialog removeCollection() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.remove_collections);
        final List<CharSequence> selectedItems = new ArrayList<>();
        Bookmarks bookmarks = Bookmarks.getInstance();
        int length = bookmarks.collectionsLength() - 1;
        CharSequence[] items = new CharSequence[length];
        CharSequence[] links = new CharSequence[length];
        boolean[] checkedItems = new boolean[length];
        for (int i = 0; i < length; i++) {
            items[i] = bookmarks.getCollectionName(i+1);
            links[i] = bookmarks.getCollectionLink(i+1);
            checkedItems[i] = false;
        }
        final CharSequence[] finalLinks = links;
        builder.setMultiChoiceItems(items, checkedItems,
                new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                CharSequence link = finalLinks[which];
                if (isChecked) {
                    selectedItems.add(link);
                } else selectedItems.remove(link);
            }
        });

        final ContentsAdapter contentsAdapter = mContentsAdapter;
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                // user clicked ok
                for (int i = selectedItems.size() - 1; i >= 0; i--) {
                    String itemLink = selectedItems.get(i).toString();
                    Log.d("OaTS", itemLink);
                    Bookmarks bookmarks = Bookmarks.getInstance();
                    bookmarks.removeCollection(itemLink);

                    List<? extends ParentListItem> parentList = contentsAdapter.getParentItemList();
                    List<Category> list = (List<Category>) parentList.get(0).getChildItemList();
                    int j = 0;
                    for (; j < list.size(); j++) {
                        if (list.get(j).getLink().equals(itemLink)) {
                            break;
                        }
                    }
                    list.remove(j);
                    //contentsAdapter.getListItem();
                    contentsAdapter.notifyChildItemRemoved(0, j);
                    bookmarks.updateFile(mContext);
                }
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // user clicked cancel
            }
        });

        return builder.create();
    }

    public void gotoSubPage() {
        Intent intent = new Intent(mContext, MainActivity.class);
        Bookmarks bookmarks = Bookmarks.getInstance();
        switch (mType) {
            case XML:
                intent = new Intent(mContext, XMLActivity.class);
                break;
            case feat:
                intent = new Intent(mContext, FeatActivity.class);
                break;
            case add_collection:
                Log.d("OaTS", "create a new collection");
                Dialog addCollectionDialog = addCollection();
                addCollectionDialog.show();
                return;
            case remove_collection:
                Log.d("OaTS", "remove a collection");
                Dialog removeCollectionDialog = removeCollection();
                removeCollectionDialog.show();
                return;
            case bookmark_collection:
                intent = new Intent(mContext, BookmarkActivity.class);
                break;
            default:
                break;
        }
        intent.putExtra(EXTRA_MESSAGE, mLink);
        intent.putExtra(EXTRA_MESSAGE_NAME, mName);
        mContext.startActivity(intent);
    }
}
