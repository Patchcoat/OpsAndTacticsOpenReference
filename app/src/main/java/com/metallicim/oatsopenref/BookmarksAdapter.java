package com.metallicim.oatsopenref;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

public class BookmarksAdapter extends RecyclerView.Adapter<BookmarksAdapter.BookmarkViewHolder> {
    public static final String EXTRA_MESSAGE = "com.metallicim.oatsopenref.MESSAGE";
    public static final String EXTRA_MESSAGE_NAME = "com.metallicim.oatsopenref.MESSAGE_NAME";

    List<Bookmarks.Bookmark> mBookmarks;
    String mCategory;

    public static class BookmarkViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout bookmarkView;
        public BookmarkViewHolder(LinearLayout v) {
            super(v);
            bookmarkView = v;
        }
    }

    public BookmarksAdapter(List<Bookmarks.Bookmark> bookmarks, String category) {
        mBookmarks = bookmarks;
        mCategory = category;
    }

    @Override
    public BookmarksAdapter.BookmarkViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LinearLayout v = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.bookmark_view, parent, false);
        return new BookmarkViewHolder(v);
    }

    public void removeBookmark(String link, String category, int position, Context context) {
        Bookmarks bookmarks = Bookmarks.getInstance();
        bookmarks.removeBookmark(category, link);
        // delete from local data
        mBookmarks.remove(position);
        // delete from view
        notifyItemRemoved(position);
        notifyItemChanged(position, mBookmarks.size());
        // update file
        bookmarks.updateFile(context);
    }

    public Dialog verifyBookmarkDelete(final String link, final int position, final Context context, boolean allBookmarks) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        CharSequence positiveButtonText = "";
        builder.setTitle(R.string.delete_bookmark);
        if (allBookmarks) {
            builder.setMessage(R.string.really_delete_bookmark);
            positiveButtonText = context.getResources().getString(R.string.delete);
        } else {
            builder.setMessage(R.string.really_remove_from_collection);
            builder.setNeutralButton(R.string.remove_from_collection, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    // user clicked remove from collection
                    removeBookmark(link, mCategory, position, context);
                }
            });
            positiveButtonText = context.getResources().getString(R.string.delete_bookmark);
        }

        builder.setPositiveButton(positiveButtonText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                // user clicked ok
                removeBookmark(link, "_all_", position, context);
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

    @Override
    public void onBindViewHolder(BookmarkViewHolder holder, int position) {
        BookmarkView bookmarkView = holder.bookmarkView.findViewById(R.id.bookmark_text_view);
        BookmarkDeleteView delete = holder.bookmarkView.findViewById(R.id.trash_icon);
        delete.setColorFilter(bookmarkView.getCurrentTextColor());
        bookmarkView.setText(mBookmarks.get(position).mName);
        final String name = mBookmarks.get(position).mName;
        final String link = mBookmarks.get(position).mLink;
        final PageType type = mBookmarks.get(position).mPageType;
        final int finalPosition = position;
        bookmarkView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        break;
                    case MotionEvent.ACTION_UP:
                        Intent intent = new Intent(view.getContext(), MainActivity.class);
                        switch (type) {
                            case XML:
                                intent = new Intent(view.getContext(), XMLActivity.class);
                                break;
                            case feat:
                                intent = new Intent(view.getContext(), FeatActivity.class);
                                break;
                            default:
                                break;
                        }
                        intent.putExtra(EXTRA_MESSAGE, link);
                        intent.putExtra(EXTRA_MESSAGE_NAME, name);
                        view.getContext().startActivity(intent);
                        view.performClick();
                        break;
                }
                return true;
            }
        });
        delete.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        break;
                    case MotionEvent.ACTION_UP:
                        Log.d("OaTS", "delete");
                        // delete from bookmarks data
                        Bookmarks bookmarks = Bookmarks.getInstance();
                        if (mCategory.equals("_all_") && bookmarks.collectionsLength() == 1) {
                            verifyBookmarkDelete(link, finalPosition, view.getContext(), true).show();
                        } else {
                            verifyBookmarkDelete(link, finalPosition, view.getContext(), false).show();
                        }
                        // this is just to keep the warnings from yelling at me
                        view.performClick();
                        break;
                }
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mBookmarks.size();
    }
}
