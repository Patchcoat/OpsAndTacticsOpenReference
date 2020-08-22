package com.metallicim.oatsopenref;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

public class BookmarksAdapter extends RecyclerView.Adapter<BookmarksAdapter.BookmarkViewHolder> {

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
        TextView v = (TextView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.bookmark_view, parent, false);

        BookmarkViewHolder vh = new BookmarkViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(BookmarkViewHolder holder, int position) {
        holder.textView.setText(mBookmarks.get(position).mName);
    }

    @Override
    public int getItemCount() {
        return mBookmarks.size();
    }
}
