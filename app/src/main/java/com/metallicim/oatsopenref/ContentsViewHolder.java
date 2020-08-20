package com.metallicim.oatsopenref;

import android.content.res.Resources;
import android.os.Build;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

public class ContentsViewHolder extends ParentViewHolder {
    private static final float INITIAL_POSITION = 0.0f;
    private static final float ROTATED_POSITION = 180f;

    private final ImageView mArrowExpandImageView;
    private TextView mCategoryTextView;

    public ContentsViewHolder(View itemView) {
        super(itemView);
        mCategoryTextView = (TextView) itemView.findViewById(R.id.contents);
        mArrowExpandImageView = (ImageView) itemView.findViewById(R.id.iv_arrow_expand);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)// this is just for colorButtonNormal
    public void bind(Contents contents) {
        mCategoryTextView.setText(contents.getName());
        Resources.Theme theme = contents.getTheme();
        TypedValue color = new TypedValue();
        theme.resolveAttribute(android.R.attr.textColor, color, true);
        mCategoryTextView.setTextColor(color.data);
        TypedValue buttonColor = new TypedValue();
        theme.resolveAttribute(android.R.attr.colorButtonNormal, buttonColor, true);
        mArrowExpandImageView.setColorFilter(buttonColor.data);
    }

    @Override
    public void setExpanded(boolean expanded) {
        super.setExpanded(expanded);

        if (expanded) {
            mArrowExpandImageView.setRotation(ROTATED_POSITION);
        } else {
            mArrowExpandImageView.setRotation(INITIAL_POSITION);
        }
    }

    @Override
    public void onExpansionToggled(boolean expanded) {
        super.onExpansionToggled(expanded);

        RotateAnimation rotateAnimation;
        float rotated_position = expanded ? ROTATED_POSITION : -1 * ROTATED_POSITION;
        rotateAnimation = new RotateAnimation(rotated_position,
                INITIAL_POSITION,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setDuration(200);
        rotateAnimation.setFillAfter(true);
        mArrowExpandImageView.startAnimation(rotateAnimation);
    }
}
