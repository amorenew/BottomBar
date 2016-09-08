package com.roughike.bottombar;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.VisibleForTesting;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.AppCompatImageView;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.TextView;

/*
 * BottomBar library for Android
 * Copyright (c) 2016 Iiro Krankka (http://github.com/roughike).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class BadgeView extends TextView {

    @VisibleForTesting
    static final String STATE_COUNT = "STATE_BADGE_COUNT_FOR_TAB_";

    private String count;

    private boolean isVisible = false;

    BadgeView(Context context) {
        super(context);
    }

    /**
     * Set the unread / new item / whatever count for this Badge.
     *
     * @param count the value this Badge should show.
     */
    void setCount(String count) {
        this.count = count;
        setText(count);
    }

    /**
     * Get the currently showing count for this Badge.
     *
     * @return current count for the Badge.
     */
    String getCount() {
        return count;
    }

    /**
     * Shows the badge with a neat little scale animation.
     */
    void show() {
        isVisible = true;
        ViewCompat.animate(this)
                .setDuration(150)
                .alpha(1)
                .scaleX(1)
                .scaleY(1)
                .start();
    }

    /**
     * Hides the badge with a neat little scale animation.
     */
    void hide() {
        isVisible = false;
        ViewCompat.animate(this)
                .setDuration(150)
                .alpha(0)
                .scaleX(0)
                .scaleY(0)
                .start();
    }

    /**
     * Is this badge currently visible?
     *
     * @return true is this badge is visible, otherwise false.
     */
    boolean isVisible() {
        return isVisible;
    }

    void attachToTab(BottomBarTab tab, int backgroundColor) {
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        setLayoutParams(params);
        setGravity(Gravity.CENTER);
        MiscUtils.setTextAppearance(this, R.style.BB_BottomBarBadge_Text);

        setColoredCircleBackground(backgroundColor);
        wrapTabAndBadgeInSameContainer(tab);
    }

    void setColoredCircleBackground(int circleColor) {
        int widthPadding = MiscUtils.dpToPixel(getContext(), 4);
        ShapeDrawable backgroundCircle = BadgeCircle.make(circleColor);
        setPadding(widthPadding, 0, widthPadding, 0);
        setBackgroundCompat(backgroundCircle);
    }

    private void wrapTabAndBadgeInSameContainer(final BottomBarTab tab) {
        ViewGroup tabContainer = (ViewGroup) tab.getParent();
        tabContainer.removeView(tab);

        final FrameLayout badgeContainer = new FrameLayout(getContext());
        badgeContainer.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        badgeContainer.addView(tab);
        badgeContainer.addView(this);

        tabContainer.addView(badgeContainer, tab.getIndexInTabContainer());

        badgeContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @SuppressWarnings("deprecation")
            @Override
            public void onGlobalLayout() {
                badgeContainer.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                adjustPositionAndSize(tab);
            }
        });
    }

    void removeFromTab(BottomBarTab tab) {
        FrameLayout badgeAndTabContainer = (FrameLayout) getParent();
        ViewGroup originalTabContainer = (ViewGroup) badgeAndTabContainer.getParent();

        badgeAndTabContainer.removeView(tab);
        originalTabContainer.removeView(badgeAndTabContainer);
        originalTabContainer.addView(tab, tab.getIndexInTabContainer());
    }

    void adjustPositionAndSize(BottomBarTab tab) {
        AppCompatImageView iconView = tab.getIconView();
        float xOffset = iconView.getWidth();

        if (tab.getType() == BottomBarTab.Type.TABLET) {
            xOffset /= 1.25;
        }

        setX(iconView.getX() + xOffset);
        setTranslationY(10);

    }

    @SuppressWarnings("deprecation")
    private void setBackgroundCompat(Drawable background) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            setBackground(background);
        } else {
            setBackgroundDrawable(background);
        }
    }

    Bundle saveState(int tabIndex) {
        Bundle state = new Bundle();
        state.putString(STATE_COUNT + tabIndex, count);
        return state;
    }

    void restoreState(Bundle bundle, int tabIndex) {
        setCount(bundle.getString(STATE_COUNT + tabIndex, count));
    }
}
