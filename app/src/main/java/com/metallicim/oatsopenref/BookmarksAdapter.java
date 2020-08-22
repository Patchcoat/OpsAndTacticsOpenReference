package com.metallicim.oatsopenref;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

public class BookmarksAdapter extends RecyclerView.Adapter<BookmarksAdapter.BookmarkViewHolder> {
    public static final String EXTRA_MESSAGE = "com.metallicim.oatsopenref.MESSAGE";
    public static final String EXTRA_MESSAGE_NAME = "com.metallicim.oatsopenref.MESSAGE_NAME";

    List<Bookmarks.Bookmark> mBookmarks;

    public static class BookmarkViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;
        public BookmarkViewHolder(TextView v) {
            super(v);
            textView = v;
        }
    }

    public BookmarksAdapter(List<Bookmarks.Bookmark> bookmarks) {
        mBookmarks = bookmarks;
    }

    @Override
    public BookmarksAdapter.BookmarkViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final TextView v = (TextView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.bookmark_view, parent, false);
        BookmarkViewHolder vh = new BookmarkViewHolder(v);
        return vh;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(BookmarkViewHolder holder, int position) {
        holder.textView.setText(mBookmarks.get(position).mName);
        final String name = mBookmarks.get(position).mName;
        final String link = mBookmarks.get(position).mLink;
        final PageType type = mBookmarks.get(position).mPageType;
        holder.textView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Log.d("OaTS", name);
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
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mBookmarks.size();
    }
}
