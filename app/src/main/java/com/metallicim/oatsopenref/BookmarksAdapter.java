package com.metallicim.oatsopenref;

import android.annotation.SuppressLint;
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

    @Override
    public void onBindViewHolder(BookmarkViewHolder holder, int position) {
        BookmarkView bookmarkView = holder.bookmarkView.findViewById(R.id.bookmark_text_view);
        BookmarkDeleteView delete = holder.bookmarkView.findViewById(R.id.trash_icon);
        delete.setColorFilter(bookmarkView.getCurrentTextColor());
        bookmarkView.setText(mBookmarks.get(position).mName);
        final String name = mBookmarks.get(position).mName;
        final String link = mBookmarks.get(position).mLink;
        final PageType type = mBookmarks.get(position).mPageType;
        final int mPosition = position;
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
                        if (link.equals("_all_") && bookmarks.collectionsLength() == 1) {
                            // TODO double check if the user really wants to delete all bookmarks
                            bookmarks.removeBookmark(mCategory, link);
                        } else {
                            bookmarks.removeBookmark(mCategory, link);
                        }
                        // delete from local data
                        mBookmarks.remove(mPosition);
                        // delete from view
                        notifyItemRemoved(mPosition);
                        notifyItemChanged(mPosition, mBookmarks.size());
                        // update file
                        bookmarks.updateFile(view.getContext());
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
