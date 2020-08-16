package com.metallicim.oatsopenref;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public class ChildViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    public ChildViewHolder(View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);
    }

    public void gotoSubPage(View v) { }

    @Override
    public void onClick(View v) {
        gotoSubPage(v);
    }
}
