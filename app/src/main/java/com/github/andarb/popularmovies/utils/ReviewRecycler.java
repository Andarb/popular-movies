package com.github.andarb.popularmovies.utils;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/** This custom `RecyclerView` class overrides `performClick()`.
 * This is required to preserve accessibility functionality when setting an `OnTouchListener` and
 * overriding `onTouch()`.
 */
public class ReviewRecycler extends RecyclerView {

    public ReviewRecycler(Context context) {
        super(context);
    }

    public ReviewRecycler(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ReviewRecycler(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }
}
