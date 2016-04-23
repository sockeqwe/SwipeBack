package com.hannesdorfmann.swipeback;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;

public class OverlaySwipeBack extends DraggableSwipeBack {

  private static final String TAG = "OverlaySwipeBack";

  private int mPeekSize;

  private final Runnable mRevealRunnable = new Runnable() {
    @Override public void run() {
      cancelContentTouch();
      int animateTo = 0;
      switch (getPosition()) {
        case RIGHT:
        case BOTTOM:
          animateTo = -mPeekSize;
          break;

        default:
          animateTo = mPeekSize;
          break;
      }
      animateOffsetTo(animateTo, 250);
    }
  };

  OverlaySwipeBack(Activity activity, int dragMode) {
    super(activity, dragMode);
  }

  public OverlaySwipeBack(Context context) {
    super(context);
  }

  public OverlaySwipeBack(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public OverlaySwipeBack(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  @TargetApi(Build.VERSION_CODES.HONEYCOMB) @Override
  protected void init(Context context, AttributeSet attrs, int defStyle) {
    super.init(context, attrs, defStyle);
    super.addView(mContentContainer, -1,
        new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    if (USE_TRANSLATIONS) {
      mContentContainer.setLayerType(View.LAYER_TYPE_NONE, null);
    }
    mContentContainer.setHardwareLayersEnabled(false);
    super.addView(mSwipeBackContainer, -1,
        new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    mPeekSize = dpToPx(20);
  }

  @Override protected void drawOverlay(Canvas canvas) {
    final int width = getWidth();
    final int height = getHeight();
    final int offsetPixels = (int) mOffsetPixels;
    final float openRatio = Math.abs(mOffsetPixels) / mSwipeBackViewSize;

    switch (getPosition()) {
      case LEFT:
        mSwipeBackOverlay.setBounds(offsetPixels, 0, width, height);
        break;

      case RIGHT:
        mSwipeBackOverlay.setBounds(0, 0, width + offsetPixels, height);
        break;

      case TOP:
        mSwipeBackOverlay.setBounds(0, offsetPixels, width, height);
        break;

      case BOTTOM:
        mSwipeBackOverlay.setBounds(0, 0, width, height + offsetPixels);
        break;
    }

    mSwipeBackOverlay.setAlpha((int) (MAX_MENU_OVERLAY_ALPHA * openRatio));
    mSwipeBackOverlay.draw(canvas);
  }

  @Override public SwipeBack open(boolean animate) {
    int animateTo = 0;
    switch (getPosition()) {
      case LEFT:
      case TOP:
        animateTo = mSwipeBackViewSize;
        break;

      case RIGHT:
      case BOTTOM:
        animateTo = -mSwipeBackViewSize;
        break;
    }

    animateOffsetTo(animateTo, 0, animate);
    return this;
  }

  @Override public SwipeBack close(boolean animate) {
    animateOffsetTo(0, 0, animate);
    return this;
  }

  @TargetApi(Build.VERSION_CODES.HONEYCOMB) @Override
  protected void onOffsetPixelsChanged(int offsetPixels) {
    if (USE_TRANSLATIONS) {
      switch (getPosition()) {
        case LEFT:
          mSwipeBackContainer.setTranslationX(offsetPixels - mSwipeBackViewSize);
          break;

        case TOP:
          mSwipeBackContainer.setTranslationY(offsetPixels - mSwipeBackViewSize);
          break;

        case RIGHT:
          mSwipeBackContainer.setTranslationX(offsetPixels + mSwipeBackViewSize);
          break;

        case BOTTOM:
          mSwipeBackContainer.setTranslationY(offsetPixels + mSwipeBackViewSize);
          break;
      }
    } else {
      switch (getPosition()) {
        case TOP:
          mSwipeBackContainer.offsetTopAndBottom(offsetPixels - mSwipeBackContainer.getBottom());
          break;

        case BOTTOM:
          mSwipeBackContainer.offsetTopAndBottom(
              offsetPixels - (mSwipeBackContainer.getTop() - getHeight()));
          break;

        case LEFT:
          mSwipeBackContainer.offsetLeftAndRight(offsetPixels - mSwipeBackContainer.getRight());
          break;

        case RIGHT:
          mSwipeBackContainer.offsetLeftAndRight(
              offsetPixels - (mSwipeBackContainer.getLeft() - getWidth()));
          break;
      }
    }

    invalidate();
  }

  @Override protected void initPeekScroller() {
    switch (getPosition()) {
      case RIGHT:
      case BOTTOM: {
        final int dx = -mPeekSize;
        mPeekScroller.startScroll(0, 0, dx, 0, PEEK_DURATION);
        break;
      }

      default: {
        final int dx = mPeekSize;
        mPeekScroller.startScroll(0, 0, dx, 0, PEEK_DURATION);
        break;
      }
    }
  }

  @Override protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    onOffsetPixelsChanged((int) mOffsetPixels);
  }

  @Override protected GradientDrawable.Orientation getDividerOrientation() {
    switch (getPosition()) {
      case TOP:
        return GradientDrawable.Orientation.TOP_BOTTOM;

      case RIGHT:
        return GradientDrawable.Orientation.RIGHT_LEFT;

      case BOTTOM:
        return GradientDrawable.Orientation.BOTTOM_TOP;

      default:
        return GradientDrawable.Orientation.LEFT_RIGHT;
    }
  }

  @Override protected void updateDividerRect() {
    final float openRatio = Math.abs(mOffsetPixels) / mSwipeBackViewSize;
    final int dropShadowSize = (int) (mDividerSize * openRatio);

    switch (getPosition()) {
      case LEFT:
        mDividerRect.top = 0;
        mDividerRect.bottom = getHeight();
        mDividerRect.left = ViewHelper.getRight(mSwipeBackContainer);
        mDividerRect.right = mDividerRect.left + dropShadowSize;
        break;

      case TOP:
        mDividerRect.left = 0;
        mDividerRect.right = getWidth();
        mDividerRect.top = ViewHelper.getBottom(mSwipeBackContainer);
        mDividerRect.bottom = mDividerRect.top + dropShadowSize;
        break;

      case RIGHT:
        mDividerRect.top = 0;
        mDividerRect.bottom = getHeight();
        mDividerRect.right = ViewHelper.getLeft(mSwipeBackContainer);
        mDividerRect.left = mDividerRect.right - dropShadowSize;
        break;

      case BOTTOM:
        mDividerRect.left = 0;
        mDividerRect.right = getWidth();
        mDividerRect.bottom = ViewHelper.getTop(mSwipeBackContainer);
        mDividerRect.top = mDividerRect.bottom - dropShadowSize;
        break;
    }
  }

  @TargetApi(Build.VERSION_CODES.HONEYCOMB) @Override protected void startLayerTranslation() {
    if (USE_TRANSLATIONS && mHardwareLayersEnabled && !mLayerTypeHardware) {
      mLayerTypeHardware = true;
      mSwipeBackContainer.setLayerType(View.LAYER_TYPE_HARDWARE, null);
    }
  }

  @TargetApi(Build.VERSION_CODES.HONEYCOMB) @Override protected void stopLayerTranslation() {
    if (mLayerTypeHardware) {
      mLayerTypeHardware = false;
      mSwipeBackContainer.setLayerType(View.LAYER_TYPE_NONE, null);
    }
  }

  @Override protected void onLayout(boolean changed, int l, int t, int r, int b) {
    final int width = r - l;
    final int height = b - t;

    mContentContainer.layout(0, 0, width, height);

    if (USE_TRANSLATIONS) {
      switch (getPosition()) {
        case LEFT:
          mSwipeBackContainer.layout(0, 0, mSwipeBackViewSize, height);
          break;

        case RIGHT:
          mSwipeBackContainer.layout(width - mSwipeBackViewSize, 0, width, height);
          break;

        case TOP:
          mSwipeBackContainer.layout(0, 0, width, mSwipeBackViewSize);
          break;

        case BOTTOM:
          mSwipeBackContainer.layout(0, height - mSwipeBackViewSize, width, height);
          break;
      }
    } else {
      final int offsetPixels = (int) mOffsetPixels;
      final int menuSize = mSwipeBackViewSize;

      switch (getPosition()) {
        case LEFT:
          mSwipeBackContainer.layout(-menuSize + offsetPixels, 0, offsetPixels, height);
          break;

        case RIGHT:
          mSwipeBackContainer.layout(width + offsetPixels, 0, width + menuSize + offsetPixels,
              height);
          break;

        case TOP:
          mSwipeBackContainer.layout(0, -menuSize + offsetPixels, width, offsetPixels);
          break;

        case BOTTOM:
          mSwipeBackContainer.layout(0, height + offsetPixels, width,
              height + menuSize + offsetPixels);
          break;
      }
    }
  }

  @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
    final int heightMode = MeasureSpec.getMode(heightMeasureSpec);

    if (widthMode == MeasureSpec.UNSPECIFIED || heightMode == MeasureSpec.UNSPECIFIED) {
      throw new IllegalStateException("Must measure with an exact size");
    }

    final int width = MeasureSpec.getSize(widthMeasureSpec);
    final int height = MeasureSpec.getSize(heightMeasureSpec);

    if (mOffsetPixels == -1) {
      open(false);
    }

    int menuWidthMeasureSpec;
    int menuHeightMeasureSpec;
    switch (getPosition()) {
      case TOP:
      case BOTTOM:
        menuWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec, 0, width);
        menuHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec, 0, mSwipeBackViewSize);
        break;

      default:
        // LEFT/RIGHT
        menuWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec, 0, mSwipeBackViewSize);
        menuHeightMeasureSpec = getChildMeasureSpec(widthMeasureSpec, 0, height);
    }
    mSwipeBackContainer.measure(menuWidthMeasureSpec, menuHeightMeasureSpec);

    final int contentWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec, 0, width);
    final int contentHeightMeasureSpec = getChildMeasureSpec(widthMeasureSpec, 0, height);
    mContentContainer.measure(contentWidthMeasureSpec, contentHeightMeasureSpec);

    setMeasuredDimension(width, height);

    updateTouchAreaSize();
  }

  private boolean isContentTouch(int x, int y) {
    boolean contentTouch = false;

    switch (getPosition()) {
      case LEFT:
        contentTouch = ViewHelper.getRight(mSwipeBackContainer) < x;
        break;

      case RIGHT:
        contentTouch = ViewHelper.getLeft(mSwipeBackContainer) > x;
        break;

      case TOP:
        contentTouch = ViewHelper.getBottom(mSwipeBackContainer) < y;
        break;

      case BOTTOM:
        contentTouch = ViewHelper.getTop(mSwipeBackContainer) > y;
        break;
    }

    return contentTouch;
  }

  protected boolean onDownAllowDrag(int x, int y) {
    switch (getPosition()) {
      case LEFT:
        return (!mSwipeBackViewVisible && mInitialMotionX <= mTouchSize) || (mSwipeBackViewVisible
            && mInitialMotionX <= mOffsetPixels);

      case RIGHT:
        final int width = getWidth();
        final int initialMotionX = (int) mInitialMotionX;

        return (!mSwipeBackViewVisible && initialMotionX >= width - mTouchSize) || (
            mSwipeBackViewVisible
                && initialMotionX >= width + mOffsetPixels);

      case TOP:
        return (!mSwipeBackViewVisible && mInitialMotionY <= mTouchSize) || (mSwipeBackViewVisible
            && mInitialMotionY <= mOffsetPixels);

      case BOTTOM:
        final int height = getHeight();
        return (!mSwipeBackViewVisible && mInitialMotionY >= height - mTouchSize) || (
            mSwipeBackViewVisible
                && mInitialMotionY >= height + mOffsetPixels);
    }

    return false;
  }

  protected boolean onMoveAllowDrag(int x, int y, float dx, float dy) {
    if (mSwipeBackViewVisible && mTouchMode == TOUCH_MODE_FULLSCREEN) {
      return true;
    }

    switch (getPosition()) {
      case LEFT:
        return (!mSwipeBackViewVisible && mInitialMotionX <= mTouchSize && (dx > 0))
            // Drawer closed
            || (mSwipeBackViewVisible && x <= mOffsetPixels)
            // Drawer open
            || (Math.abs(mOffsetPixels) <= mPeekSize && mSwipeBackViewVisible); // Drawer revealed

      case RIGHT:
        final int width = getWidth();
        return (!mSwipeBackViewVisible && mInitialMotionX >= width - mTouchSize && (dx < 0)) || (
            mSwipeBackViewVisible
                && x >= width - mOffsetPixels) || (Math.abs(mOffsetPixels) <= mPeekSize
            && mSwipeBackViewVisible);

      case TOP:
        return (!mSwipeBackViewVisible && mInitialMotionY <= mTouchSize && (dy > 0)) || (
            mSwipeBackViewVisible
                && x <= mOffsetPixels) || (Math.abs(mOffsetPixels) <= mPeekSize
            && mSwipeBackViewVisible);

      case BOTTOM:
        final int height = getHeight();
        return (!mSwipeBackViewVisible && mInitialMotionY >= height - mTouchSize && (dy < 0)) || (
            mSwipeBackViewVisible
                && x >= height - mOffsetPixels) || (Math.abs(mOffsetPixels) <= mPeekSize
            && mSwipeBackViewVisible);
    }

    return false;
  }

  protected void onMoveEvent(float dx, float dy) {
    switch (getPosition()) {
      case LEFT:
        setOffsetPixels(Math.min(Math.max(mOffsetPixels + dx, 0), mSwipeBackViewSize));
        break;

      case RIGHT:
        setOffsetPixels(Math.max(Math.min(mOffsetPixels + dx, 0), -mSwipeBackViewSize));
        break;

      case TOP:
        setOffsetPixels(Math.min(Math.max(mOffsetPixels + dy, 0), mSwipeBackViewSize));
        break;

      case BOTTOM:
        setOffsetPixels(Math.max(Math.min(mOffsetPixels + dy, 0), -mSwipeBackViewSize));
        break;
    }
  }

  protected void onUpEvent(int x, int y) {
    final int offsetPixels = (int) mOffsetPixels;

    switch (getPosition()) {
      case LEFT: {
        if (mIsDragging) {
          mVelocityTracker.computeCurrentVelocity(1000, mMaxVelocity);
          final int initialVelocity = (int) getXVelocity(mVelocityTracker);
          mLastMotionX = x;
          animateOffsetTo(initialVelocity > 0 ? mSwipeBackViewSize : 0, initialVelocity, true);

          // Close the menu when content is clicked while the menu is visible.
        } else if (mSwipeBackViewVisible) {
          close();
        }
        break;
      }

      case TOP: {
        if (mIsDragging) {
          mVelocityTracker.computeCurrentVelocity(1000, mMaxVelocity);
          final int initialVelocity = (int) getYVelocity(mVelocityTracker);
          mLastMotionY = y;
          animateOffsetTo(initialVelocity > 0 ? mSwipeBackViewSize : 0, initialVelocity, true);

          // Close the menu when content is clicked while the menu is visible.
        } else if (mSwipeBackViewVisible) {
          close();
        }
        break;
      }

      case RIGHT: {
        final int width = getWidth();

        if (mIsDragging) {
          mVelocityTracker.computeCurrentVelocity(1000, mMaxVelocity);
          final int initialVelocity = (int) getXVelocity(mVelocityTracker);
          mLastMotionX = x;
          animateOffsetTo(initialVelocity > 0 ? 0 : -mSwipeBackViewSize, initialVelocity, true);

          // Close the menu when content is clicked while the menu is visible.
        } else if (mSwipeBackViewVisible) {
          close();
        }
        break;
      }

      case BOTTOM: {
        if (mIsDragging) {
          mVelocityTracker.computeCurrentVelocity(1000, mMaxVelocity);
          final int initialVelocity = (int) getYVelocity(mVelocityTracker);
          mLastMotionY = y;
          animateOffsetTo(initialVelocity < 0 ? -mSwipeBackViewSize : 0, initialVelocity, true);

          // Close the menu when content is clicked while the menu is visible.
        } else if (mSwipeBackViewVisible) {
          close();
        }
        break;
      }
    }
  }

  protected boolean checkTouchSlop(float dx, float dy) {
    switch (getPosition()) {
      case TOP:
      case BOTTOM:
        return Math.abs(dy) > mTouchSlop && Math.abs(dy) > Math.abs(dx);

      default:
        return Math.abs(dx) > mTouchSlop && Math.abs(dx) > Math.abs(dy);
    }
  }

  @Override protected void stopAnimation() {
    super.stopAnimation();
    removeCallbacks(mRevealRunnable);
  }

  @Override public boolean onInterceptTouchEvent(MotionEvent ev) {
    final int action = ev.getAction() & MotionEvent.ACTION_MASK;

    if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
      removeCallbacks(mRevealRunnable);
      mActivePointerId = INVALID_POINTER;
      mIsDragging = false;
      if (mVelocityTracker != null) {
        mVelocityTracker.recycle();
        mVelocityTracker = null;
      }

      if (Math.abs(mOffsetPixels) > mSwipeBackViewSize / 2) {
        open();
      } else {
        close();
      }

      return false;
    }

    if (action == MotionEvent.ACTION_DOWN && mSwipeBackViewVisible && isCloseEnough()) {
      setOffsetPixels(0);
      stopAnimation();
      endPeek();
      setDrawerState(STATE_CLOSED);
      mIsDragging = false;
    }

    // Always intercept events over the content while menu is visible.
    if (mSwipeBackViewVisible) {
      int index = 0;
      if (mActivePointerId != INVALID_POINTER) {
        index = ev.findPointerIndex(mActivePointerId);
        index = index == -1 ? 0 : index;
      }

      final int x = (int) ev.getX(index);
      final int y = (int) ev.getY(index);
      if (isContentTouch(x, y)) {
        return true;
      }
    }

    if (!mSwipeBackViewVisible && !mIsDragging && mTouchMode == TOUCH_MODE_NONE) {
      return false;
    }

    if (action != MotionEvent.ACTION_DOWN && mIsDragging) {
      return true;
    }

    switch (action) {
      case MotionEvent.ACTION_DOWN: {
        mLastMotionX = mInitialMotionX = ev.getX();
        mLastMotionY = mInitialMotionY = ev.getY();
        final boolean allowDrag = onDownAllowDrag((int) mLastMotionX, (int) mLastMotionY);
        mActivePointerId = ev.getPointerId(0);

        if (allowDrag) {
          setDrawerState(mSwipeBackViewVisible ? STATE_OPEN : STATE_CLOSED);
          stopAnimation();
          endPeek();

          if (!mSwipeBackViewVisible && mInitialMotionX <= mPeekSize) {
            postDelayed(mRevealRunnable, 160);
          }

          mIsDragging = false;
        }
        break;
      }

      case MotionEvent.ACTION_MOVE: {
        final int activePointerId = mActivePointerId;
        if (activePointerId == INVALID_POINTER) {
          // If we don't have a valid id, the touch down wasn't on content.
          break;
        }

        final int pointerIndex = ev.findPointerIndex(activePointerId);
        if (pointerIndex == -1) {
          mIsDragging = false;
          mActivePointerId = INVALID_POINTER;
          endDrag();
          close(true);
          return false;
        }

        final float x = ev.getX(pointerIndex);
        final float dx = x - mLastMotionX;
        final float y = ev.getY(pointerIndex);
        final float dy = y - mLastMotionY;

        if (Math.abs(dx) >= mTouchSlop || Math.abs(dy) >= mTouchSlop) {
          removeCallbacks(mRevealRunnable);
          endPeek();
        }

        if (checkTouchSlop(dx, dy)) {
          if (mOnInterceptMoveEventListener != null && (mTouchMode == TOUCH_MODE_FULLSCREEN
              || mSwipeBackViewVisible) && canChildrenScroll((int) dx, (int) dy, (int) x,
              (int) y)) {
            endDrag(); // Release the velocity tracker
            requestDisallowInterceptTouchEvent(true);
            return false;
          }

          final boolean allowDrag = onMoveAllowDrag((int) x, (int) y, dx, dy);

          if (allowDrag) {
            endPeek();
            stopAnimation();
            setDrawerState(STATE_DRAGGING);
            mIsDragging = true;
            mLastMotionX = x;
            mLastMotionY = y;
          }
        }
        break;
      }

      case MotionEvent.ACTION_POINTER_UP:
        onPointerUp(ev);
        mLastMotionX = safeGetX(ev);
        mLastMotionY = safeGetY(ev);
        break;
    }

    if (mVelocityTracker == null) {
      mVelocityTracker = VelocityTracker.obtain();
    }
    mVelocityTracker.addMovement(ev);

    return mIsDragging;
  }

  @Override public boolean onTouchEvent(MotionEvent ev) {
    if (!mSwipeBackViewVisible && !mIsDragging && mTouchMode == TOUCH_MODE_NONE) {
      return false;
    }
    final int action = ev.getAction() & MotionEvent.ACTION_MASK;

    if (mVelocityTracker == null) {
      mVelocityTracker = VelocityTracker.obtain();
    }
    mVelocityTracker.addMovement(ev);

    switch (action) {
      case MotionEvent.ACTION_DOWN: {
        mLastMotionX = mInitialMotionX = ev.getX();
        mLastMotionY = mInitialMotionY = ev.getY();
        final boolean allowDrag = onDownAllowDrag((int) mLastMotionX, (int) mLastMotionY);

        mActivePointerId = ev.getPointerId(0);

        if (allowDrag) {
          stopAnimation();
          endPeek();

          if (!mSwipeBackViewVisible && mLastMotionX <= mPeekSize) {
            postDelayed(mRevealRunnable, 160);
          }

          startLayerTranslation();
        }
        break;
      }

      case MotionEvent.ACTION_MOVE: {
        final int pointerIndex = ev.findPointerIndex(mActivePointerId);
        if (pointerIndex == -1) {
          mIsDragging = false;
          mActivePointerId = INVALID_POINTER;
          endDrag();
          close(true);
          return false;
        }

        if (!mIsDragging) {
          final float x = ev.getX(pointerIndex);
          final float dx = x - mLastMotionX;
          final float y = ev.getY(pointerIndex);
          final float dy = y - mLastMotionY;

          if (checkTouchSlop(dx, dy)) {
            final boolean allowDrag = onMoveAllowDrag((int) x, (int) y, dx, dy);

            if (allowDrag) {
              endPeek();
              stopAnimation();
              setDrawerState(STATE_DRAGGING);
              mIsDragging = true;
              mLastMotionX = x;
              mLastMotionY = y;
            } else {
              mInitialMotionX = x;
              mInitialMotionY = y;
            }
          }
        }

        if (mIsDragging) {
          startLayerTranslation();

          final float x = ev.getX(pointerIndex);
          final float dx = x - mLastMotionX;
          final float y = ev.getY(pointerIndex);
          final float dy = y - mLastMotionY;

          mLastMotionX = x;
          mLastMotionY = y;
          onMoveEvent(dx, dy);
        }
        break;
      }

      case MotionEvent.ACTION_CANCEL:
      case MotionEvent.ACTION_UP: {
        removeCallbacks(mRevealRunnable);
        int index = ev.findPointerIndex(mActivePointerId);
        index = index == -1 ? 0 : index;
        final int x = (int) ev.getX(index);
        final int y = (int) ev.getY(index);
        onUpEvent(x, y);
        mActivePointerId = INVALID_POINTER;
        mIsDragging = false;
        break;
      }

      case MotionEvent.ACTION_POINTER_DOWN:
        final int index = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK)
            >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
        mLastMotionX = ev.getX(index);
        mLastMotionY = ev.getY(index);
        mActivePointerId = ev.getPointerId(index);
        break;

      case MotionEvent.ACTION_POINTER_UP:
        onPointerUp(ev);
        mLastMotionX = ev.getX(ev.findPointerIndex(mActivePointerId));
        mLastMotionY = ev.getY(ev.findPointerIndex(mActivePointerId));
        break;
    }

    return true;
  }

  private void onPointerUp(MotionEvent ev) {
    final int pointerIndex = ev.getActionIndex();
    final int pointerId = ev.getPointerId(pointerIndex);
    if (pointerId == mActivePointerId) {
      final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
      mLastMotionX = ev.getX(newPointerIndex);
      mActivePointerId = ev.getPointerId(newPointerIndex);
      if (mVelocityTracker != null) {
        mVelocityTracker.clear();
      }
    }
  }
}
