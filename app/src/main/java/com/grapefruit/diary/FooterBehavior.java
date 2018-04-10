package com.grapefruit.diary;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;

public class FooterBehavior extends CoordinatorLayout.Behavior<View> {

    private int mTotalDyDistance;
    private boolean hide = false;
    private int childHeight;

    public FooterBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onStartNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull View child, @NonNull View directTargetChild, @NonNull View target, int axes, int type) {
        childHeight = child.getHeight();
        return (axes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
    }

    @Override
    public void onNestedPreScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull View child, @NonNull View target, int dx, int dy, @NonNull int[] consumed, int type) {
        if (dy > 0 && mTotalDyDistance < 0 || dy < 0 && mTotalDyDistance > 0) {
            mTotalDyDistance = 0;
        }
        mTotalDyDistance += dy;
        if (!hide && mTotalDyDistance > -child.getHeight()) {
            hideView(child);
            hide = true;
        } else if (hide && mTotalDyDistance < -child.getHeight()) {
            showView(child);
            hide = false;
        }
    }

    private void hideView(final View child) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(child, "translationY", 0, childHeight);
        animator.setDuration(300);
        animator.start();
    }

    private void showView(final View child) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(child, "translationY", childHeight, 0);
        animator.setDuration(300);
        animator.start();
    }
}
