package com.hannesdorfmann.swipeback;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;

public class SlidingSwipeBack extends DraggableSwipeBack {

	private static final String TAG = "OverlaySwipeBack";

	SlidingSwipeBack(Activity activity, int dragMode) {
		super(activity, dragMode);
	}

	public SlidingSwipeBack(Context context) {
		super(context);
	}

	public SlidingSwipeBack(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SlidingSwipeBack(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void init(Context context, AttributeSet attrs, int defStyle) {
		super.init(context, attrs, defStyle);
		super.addView(mSwipeBackContainer, -1, new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		super.addView(mContentContainer, -1, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
	}

	@Override
	public SwipeBack open(boolean animate) {
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

	@Override
	public SwipeBack close(boolean animate) {
		animateOffsetTo(0, 0, animate);
		return this;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	protected void onOffsetPixelsChanged(int offsetPixels) {
		if (USE_TRANSLATIONS) {
			switch (getPosition()) {
			case TOP:
			case BOTTOM:
				mContentContainer.setTranslationY(offsetPixels);
				break;

			default:
				mContentContainer.setTranslationX(offsetPixels);
				break;
			}
		} else {
			switch (getPosition()) {
			case TOP:
			case BOTTOM:
				mContentContainer.offsetTopAndBottom(offsetPixels - mContentContainer.getTop());
				break;

			default:
				mContentContainer.offsetLeftAndRight(offsetPixels - mContentContainer.getLeft());
				break;
			}
		}

		offsetMenu(offsetPixels);
		invalidate();
	}

	@Override
	protected void initPeekScroller() {
		switch (getPosition()) {
		case RIGHT:
		case BOTTOM: {
			final int dx = -mSwipeBackViewSize / 3;
			mPeekScroller.startScroll(0, 0, dx, 0, PEEK_DURATION);
			break;
		}

		default: {
			final int dx = mSwipeBackViewSize / 3;
			mPeekScroller.startScroll(0, 0, dx, 0, PEEK_DURATION);
			break;
		}
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		onOffsetPixelsChanged((int) mOffsetPixels);
	}

	@Override
	protected void drawOverlay(Canvas canvas) {
		final int width = getWidth();
		final int height = getHeight();
		final int offsetPixels = (int) mOffsetPixels;
		final float openRatio = Math.abs(mOffsetPixels) / mSwipeBackViewSize;

		switch (getPosition()) {
		case LEFT:
			mSwipeBackOverlay.setBounds(0, 0, offsetPixels, height);
			break;

		case RIGHT:
			mSwipeBackOverlay.setBounds(width + offsetPixels, 0, width, height);
			break;

		case TOP:
			mSwipeBackOverlay.setBounds(0, 0, width, offsetPixels);
			break;

		case BOTTOM:
			mSwipeBackOverlay.setBounds(0, height + offsetPixels, width, height);
			break;
		}

		mSwipeBackOverlay.setAlpha((int) (MAX_MENU_OVERLAY_ALPHA * (1.f - openRatio)));
		mSwipeBackOverlay.draw(canvas);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		final int width = r - l;
		final int height = b - t;

		if (USE_TRANSLATIONS) {
			mContentContainer.layout(0, 0, width, height);
		} else {
			final int offsetPixels = (int) mOffsetPixels;
			if (getPosition() == Position.LEFT || getPosition() == Position.RIGHT) {
				mContentContainer.layout(offsetPixels, 0, width + offsetPixels, height);
			} else {
				mContentContainer.layout(0, offsetPixels, width, height + offsetPixels);
			}
		}

		switch (getPosition()) {
		case LEFT:
			mSwipeBackContainer.layout(0, 0, mSwipeBackViewSize, height);
			break;

		case RIGHT:
			mSwipeBackContainer.layout(width - mSwipeBackViewSize, 0, width,
					height);
			break;

		case TOP:
			mSwipeBackContainer.layout(0, 0, width, mSwipeBackViewSize);
			break;

		case BOTTOM:
			mSwipeBackContainer.layout(0, height - mSwipeBackViewSize, width,
					height);
			break;
		}
	}

	/**
	 * Offsets the menu relative to its original position based on the position of the content.
	 *
	 * @param offsetPixels The number of pixels the content if offset.
	 */
	 @TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void offsetMenu(int offsetPixels) {
		 if (!mOffsetMenu || mSwipeBackViewSize == 0) {
			 return;
		 }

		 final int width = getWidth();
		 final int height = getHeight();

		 final int menuSize = mSwipeBackViewSize;
		 final int sign = (int) (mOffsetPixels / Math.abs(mOffsetPixels));
		 final float openRatio = Math.abs(mOffsetPixels) / menuSize;
		 final int offset = (int) (-0.25f * ((1.0f - openRatio) * menuSize) * sign);

		 switch (getPosition()) {
		 case LEFT: {
			 if (USE_TRANSLATIONS) {
				 if (offsetPixels > 0) {
					mSwipeBackContainer.setTranslationX(offset);
				 } else {
					mSwipeBackContainer.setTranslationX(-menuSize);
				 }

			 } else {
				mSwipeBackContainer.offsetLeftAndRight(offset
						- mSwipeBackContainer.getLeft());
				mSwipeBackContainer.setVisibility(offsetPixels == 0 ? INVISIBLE
						: VISIBLE);
			 }
			 break;
		 }

		 case RIGHT: {
			 if (USE_TRANSLATIONS) {
				 if (offsetPixels != 0) {
					mSwipeBackContainer.setTranslationX(offset);
				 } else {
					mSwipeBackContainer.setTranslationX(menuSize);
				 }

			 } else {
				final int oldOffset = mSwipeBackContainer.getRight() - width;
				 final int offsetBy = offset - oldOffset;
				mSwipeBackContainer.offsetLeftAndRight(offsetBy);
				mSwipeBackContainer.setVisibility(offsetPixels == 0 ? INVISIBLE
						: VISIBLE);
			 }
			 break;
		 }

		 case TOP: {
			 if (USE_TRANSLATIONS) {
				 if (offsetPixels > 0) {
					mSwipeBackContainer.setTranslationY(offset);
				 } else {
					mSwipeBackContainer.setTranslationY(-menuSize);
				 }

			 } else {
				mSwipeBackContainer.offsetTopAndBottom(offset
						- mSwipeBackContainer.getTop());
				mSwipeBackContainer.setVisibility(offsetPixels == 0 ? INVISIBLE
						: VISIBLE);
			 }
			 break;
		 }

		 case BOTTOM: {
			 if (USE_TRANSLATIONS) {
				 if (offsetPixels != 0) {
					mSwipeBackContainer.setTranslationY(offset);
				 } else {
					mSwipeBackContainer.setTranslationY(menuSize);
				 }

			 } else {
				final int oldOffset = mSwipeBackContainer.getBottom() - height;
				 final int offsetBy = offset - oldOffset;
				mSwipeBackContainer.offsetTopAndBottom(offsetBy);
				mSwipeBackContainer.setVisibility(offsetPixels == 0 ? INVISIBLE
						: VISIBLE);
			 }
			 break;
		 }
		 }

	 }

	 @Override
	 protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
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
		mSwipeBackContainer
				.measure(menuWidthMeasureSpec, menuHeightMeasureSpec);

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
			 contentTouch = ViewHelper.getLeft(mContentContainer) < x;
			 break;

		 case RIGHT:
			 contentTouch = ViewHelper.getRight(mContentContainer) > x;
			 break;

		 case TOP:
			 contentTouch = ViewHelper.getTop(mContentContainer) < y;
			 break;

		 case BOTTOM:
			 contentTouch = ViewHelper.getBottom(mContentContainer) > y;
			 break;
		 }

		 return contentTouch;
	 }

	 protected boolean onDownAllowDrag(int x, int y) {
		 switch (getPosition()) {
		 case LEFT:
			 return (!mSwipeBackViewVisible && mInitialMotionX <= mTouchSize)
					 || (mSwipeBackViewVisible && mInitialMotionX >= mOffsetPixels);

		 case RIGHT:
			 final int width = getWidth();
			 final int initialMotionX = (int) mInitialMotionX;

			 return (!mSwipeBackViewVisible && initialMotionX >= width - mTouchSize)
					 || (mSwipeBackViewVisible && initialMotionX <= width + mOffsetPixels);

		 case TOP:
			 return (!mSwipeBackViewVisible && mInitialMotionY <= mTouchSize)
					 || (mSwipeBackViewVisible && mInitialMotionY >= mOffsetPixels);

		 case BOTTOM:
			 final int height = getHeight();
			 return (!mSwipeBackViewVisible && mInitialMotionY >= height - mTouchSize)
					 || (mSwipeBackViewVisible && mInitialMotionY <= height + mOffsetPixels);
		 }

		 return false;
	 }

	 protected boolean onMoveAllowDrag(int x, int y, float dx, float dy) {
		 switch (getPosition()) {
		 case LEFT:
			 return (!mSwipeBackViewVisible && mInitialMotionX <= mTouchSize && (dx > 0))
					 || (mSwipeBackViewVisible && x >= mOffsetPixels);

		 case RIGHT:
			 final int width = getWidth();
			 return (!mSwipeBackViewVisible && mInitialMotionX >= width - mTouchSize && (dx < 0))
					 || (mSwipeBackViewVisible && x <= width + mOffsetPixels);

		 case TOP:
			 return (!mSwipeBackViewVisible && mInitialMotionY <= mTouchSize && (dy > 0))
					 || (mSwipeBackViewVisible && y >= mOffsetPixels);

		 case BOTTOM:
			 final int height = getHeight();
			 return (!mSwipeBackViewVisible && mInitialMotionY >= height - mTouchSize && (dy < 0))
					 || (mSwipeBackViewVisible && y <= height + mOffsetPixels);
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
			 } else if (mSwipeBackViewVisible && x > offsetPixels) {
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
			 } else if (mSwipeBackViewVisible && y > offsetPixels) {
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
			 } else if (mSwipeBackViewVisible && x < width + offsetPixels) {
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
			 } else if (mSwipeBackViewVisible && y < getHeight() + offsetPixels) {
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

	 @Override
	 public boolean onInterceptTouchEvent(MotionEvent ev) {
		 final int action = ev.getAction() & MotionEvent.ACTION_MASK;

		 if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
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

			 if (checkTouchSlop(dx, dy)) {
				 if (mOnInterceptMoveEventListener != null && (mTouchMode == TOUCH_MODE_FULLSCREEN || mSwipeBackViewVisible)
						 && canChildrenScroll((int) dx, (int) dy, (int) x, (int) y)) {
					 endDrag(); // Release the velocity tracker
					 requestDisallowInterceptTouchEvent(true);
					 return false;
				 }

				 final boolean allowDrag = onMoveAllowDrag((int) x, (int) y, dx, dy);

				 if (allowDrag) {
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
			 mLastMotionX = ev.getX(ev.findPointerIndex(mActivePointerId));
			 mLastMotionY = ev.getY(ev.findPointerIndex(mActivePointerId));
			 break;
		 }

		 if (mVelocityTracker == null) {
			 mVelocityTracker = VelocityTracker.obtain();
		 }
		 mVelocityTracker.addMovement(ev);

		 return mIsDragging;
	 }

	 @Override
	 public boolean onTouchEvent(MotionEvent ev) {
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
