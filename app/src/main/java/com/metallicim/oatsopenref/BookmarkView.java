package com.metallicim.oatsopenref;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.appcompat.widget.AppCompatTextView;

public class BookmarkView extends AppCompatTextView {

    public BookmarkView(Context context) {
        super(context);
    }

    public BookmarkView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        return false;
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

}
