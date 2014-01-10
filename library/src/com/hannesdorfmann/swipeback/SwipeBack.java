package com.hannesdorfmann.swipeback;



import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.animation.Interpolator;

import com.hannesdorfmann.swipeback.interpolator.SmoothInterpolator;
import com.hannesdorfmann.swipeback.transformer.DefaultSwipeBackTransformer;
import com.hannesdorfmann.swipeback.transformer.SwipeBackTransformer;

public abstract class SwipeBack extends ViewGroup {

	/**
	 * Callback interface for changing state of the swipe back.
	 */
	public interface OnStateChangeListener {

		/**
		 * Called when the internal state has changed.
		 * 
		 * @param oldState
		 *            The old drawer state.
		 * @param newState
		 *            The new drawer state.
		 */
		void onStateChanged(int oldState, int newState);

		/**
		 * Called when the swipe back slides.
		 * 
		 * @param openRatio
		 *            Ratio for how open the swipe back view is.
		 * @param offsetPixels
		 *            Current offset of the swipe back view in pixels.
		 */
		void onSlide(float openRatio, int offsetPixels);
	}

	/**
	 * Callback that is invoked when the drawer is in the process of deciding whether it should intercept the touch
	 * event. This lets the listener decide if the pointer is on a view that would disallow dragging of the drawer.
	 * This is only called when the touch mode is {@link #TOUCH_MODE_FULLSCREEN}.
	 */
	public interface OnInterceptMoveEventListener {

		/**
		 * Called for each child the pointer i on when the drawer is deciding whether to intercept the touch event.
		 *
		 * @param v     View to test for draggability
		 * @param delta Delta drag in pixels
		 * @param x     X coordinate of the active touch point
		 * @param y     Y coordinate of the active touch point
		 * @return true if view is draggable by delta dx.
		 */
		boolean isViewDraggable(View v, int delta, int x, int y);
	}

	public enum Type {
		/**
		 * Positions the drawer behind the content.
		 */
		BEHIND,

		// /**
		// * A static drawer that can not be dragged.
		// */
		//STATIC,

		/**
		 * Positions the drawer on top of the content.
		 */
		OVERLAY,
	}

	/**
	 * Tag used when logging.
	 */
	private static final String TAG = "SwipeBack";

	/**
	 * Indicates whether debug code should be enabled.
	 */
	private static final boolean DEBUG = false;

	/**
	 * The time between each frame when animating the drawer.
	 */
	protected static final int ANIMATION_DELAY = 1000 / 60;

	/**
	 * The default touch bezel size of the drawer in dp.
	 */
	private static final int DEFAULT_DRAG_BEZEL_DP = 24;

	/**
	 * The default drop shadow size in dp.
	 */
	private static final int DEFAULT_DIVIDER_SIZE_DP = 6;

	/**
	 * Drag mode for sliding only the content view.
	 */
	public static final int DRAG_CONTENT = 0;

	/**
	 * Drag mode for sliding the entire window.
	 */
	public static final int DRAG_WINDOW = 1;

	/**
	 * Disallow opening the drawer by dragging the screen.
	 */
	public static final int TOUCH_MODE_NONE = 0;

	/**
	 * Allow opening drawer only by dragging on the edge of the screen.
	 */
	public static final int TOUCH_MODE_BEZEL = 1;

	/**
	 * Allow opening drawer by dragging anywhere on the screen.
	 */
	public static final int TOUCH_MODE_FULLSCREEN = 2;

	/**
	 * Indicates that the drawer is currently closed.
	 */
	public static final int STATE_CLOSED = 0;

	/**
	 * Indicates that the drawer is currently closing.
	 */
	public static final int STATE_CLOSING = 1;

	/**
	 * Indicates that the drawer is currently being dragged by the user.
	 */
	public static final int STATE_DRAGGING = 2;

	/**
	 * Indicates that the drawer is currently opening.
	 */
	public static final int STATE_OPENING = 4;

	/**
	 * Indicates that the drawer is currently open.
	 */
	public static final int STATE_OPEN = 8;

	/**
	 * Indicates whether to use {@link android.view.View#setTranslationX(float)} when positioning views.
	 */
	static final boolean USE_TRANSLATIONS = Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;


	/**
	 * The maximum animation duration.
	 */
	private static final int DEFAULT_ANIMATION_DURATION = 500;

	/**
	 * Interpolator used when animating the drawer open/closed.
	 */
	protected static final Interpolator SMOOTH_INTERPOLATOR = new SmoothInterpolator();


	/**
	 * The default size of the swipe back view in dp
	 */
	private static final int DEFAULT_SIZE = 50;

	/**
	 * Drawable used as swipe back view overlay.
	 */
	protected Drawable mSwipeBackOverlay;

	/**
	 * Defines if the divider is enabled (will be drawn)
	 */
	protected boolean mDividerEnabled;

	/**
	 * The color of the shadow that will be used as divider .
	 */
	protected int mDividerAsShadowColor;

	/**
	 * Drawable used as content to swipe back view divider.
	 */
	protected Drawable mDividerDrawable;

	private boolean mCustomDivider;

	/**
	 * The size (width) of the content to swipe back view divider.
	 */
	protected int mDividerSize;

	/**
	 * The currently active view.
	 */
	protected View mActiveView;

	/**
	 * Position of the active view. This is compared to View#getTag(R.id.mdActiveViewPosition) when drawing the
	 * indicator.
	 */
	protected int mActivePosition;

	/**
	 * Used for storing the previous touch mode, while disabling Swipe Back
	 * temporarly
	 */
	protected int mPreviousTouchMode;

	/**
	 * Used when reading the position of the active view.
	 */
	protected final Rect mActiveRect = new Rect();

	/**
	 * Temporary {@link android.graphics.Rect} used for deciding whether the view should be invalidated so the indicator can be redrawn.
	 */
	private final Rect mTempRect = new Rect();

	/**
	 * The custom swipe back view set by the user.
	 */
	private View mSwipeBackView;

	/**
	 * The parent of the swipe back view.
	 */
	protected BuildLayerFrameLayout mSwipeBackContainer;

	/**
	 * The parent of the content view.
	 */
	protected BuildLayerFrameLayout mContentContainer;

	/**
	 * The size of the swipe back view (width or height depending on the
	 * gravity).
	 */
	protected int mSwipeBackViewSize;

	/**
	 * Indicates whether the swipe back view is currently visible.
	 */
	protected boolean mSwipeBackViewVisible;

	/**
	 * The drag mode of the drawer. Can be either {@link #DRAG_CONTENT} or {@link #DRAG_WINDOW}.
	 */
	private int mDragMode = DRAG_WINDOW;

	/**
	 * The current drawer state.
	 *
	 * @see #STATE_CLOSED
	 * @see #STATE_CLOSING
	 * @see #STATE_DRAGGING
	 * @see #STATE_OPENING
	 * @see #STATE_OPEN
	 */
	protected int mDrawerState = STATE_CLOSED;

	/**
	 * The touch bezel size of the swipe back in px.
	 */
	protected int mTouchBezelSize;

	/**
	 * The touch area size of the swipe back in px.
	 */
	protected int mTouchSize;

	/**
	 * Listener used to dispatch state change events.
	 */
	private OnStateChangeListener mOnStateChangeListener;

	/**
	 * An additional {@link OnStateChangeListener} which will be plugged in to
	 * the internal delegate to allow to observe the internal state from outside
	 */
	private OnStateChangeListener mAdditionalOnStateChangeListener;

	/**
	 * Touch mode for the Drawer.
	 * Possible values are {@link #TOUCH_MODE_NONE}, {@link #TOUCH_MODE_BEZEL} or {@link #TOUCH_MODE_FULLSCREEN}
	 * Default: {@link #TOUCH_MODE_BEZEL}
	 */
	protected int mTouchMode = TOUCH_MODE_FULLSCREEN;

	/**
	 * Indicates whether to use {@link android.view.View#LAYER_TYPE_HARDWARE} when animating the drawer.
	 */
	protected boolean mHardwareLayersEnabled = true;

	/**
	 * The Activity the drawer is attached to.
	 */
	private Activity mActivity;

	/**
	 * Bundle used to hold the drawers state.
	 */
	protected Bundle mState;

	/**
	 * The maximum duration of open/close animations.
	 */
	protected int mMaxAnimationDuration = DEFAULT_ANIMATION_DURATION;

	/**
	 * Callback that lets the listener override intercepting of touch events.
	 */
	protected OnInterceptMoveEventListener mOnInterceptMoveEventListener;

	/**
	 * The position of the drawer.
	 */
	private Position mPosition;

	private Position mResolvedPosition;


	protected boolean mIsStatic = false;

	protected final Rect mDividerRect = new Rect();

	/**
	 * Current offset.
	 */
	protected float mOffsetPixels;

	/**
	 * Whether an overlay should be drawn as the drawer is opened and closed.
	 */
	protected boolean mDrawOverlay;

	/**
	 * The SwipeBackTransformer
	 */
	protected SwipeBackTransformer mSwipeBackTransformer;

	/**
	 * Attaches the SwipeBack to the Activity.
	 *
	 * @param activity The activity that the SwipeBack will be attached to.
	 * @return The created SwipeBack instance.
	 */
	public static SwipeBack attach(Activity activity) {
		return attach(activity, Type.BEHIND);
	}

	/**
	 * Attaches the SwipeBack to the Activity.
	 *
	 * @param activity The activity that the SwipeBack will be attached to.
	 * @param  transformer the transformer
	 * @return The created SwipeBack instance.
	 */
	public static SwipeBack attach(Activity activity, SwipeBackTransformer transformer) {
		return attach(activity, Type.BEHIND, transformer);
	}

	/**
	 * Attaches the SwipeBack to the Activity.
	 * 
	 * @param activity
	 *            The activity the Swipe Back will be attached to.
	 * @param type
	 *            The {@link SwipeBack.Type} of the drawer.
	 * @return The created SwipeBack instance.
	 */
	public static SwipeBack attach(Activity activity, Type type) {
		return attach(activity, type, Position.START);
	}

	/**
	 * Attaches the SwipeBack to the Activity.
	 * 
	 * @param activity
	 *            The activity the swipe back will be attached to.
	 * @param type
	 *            The {@link SwipeBack.Type} of the drawer.
	 * @return The created SwipeBack instance.
	 */
	public static SwipeBack attach(Activity activity, Type type, SwipeBackTransformer transformer) {
		return attach(activity, type, Position.START);
	}


	/**
	 * Attaches the SwipeBack to the Activity.
	 * 
	 * @param activity
	 *            The activity the swipe back will be attached to.
	 * @param position
	 *            Where to position the swipe back.
	 * @return The created SwipeBack instance.
	 */
	public static SwipeBack attach(Activity activity, Position position, SwipeBackTransformer transformer) {
		return attach(activity, Type.BEHIND, position, transformer);
	}


	/**
	 * Attaches the SwipeBack to the Activity.
	 * 
	 * @param activity
	 *            The activity the swipe back will be attached to.
	 * @param position
	 *            Where to position the swipe back.
	 * @return The created SwipeBack instance.
	 */
	public static SwipeBack attach(Activity activity, Position position) {
		return attach(activity, Type.BEHIND, position);
	}



	/**
	 * Attaches the SwipeBack to the Activity.
	 * 
	 * @param activity
	 *            The activity the swipe back will be attached to.
	 * @param type
	 *            The {@link SwipeBack.Type} of the drawer.
	 * @param position
	 *            Where to position the swipe back.
	 * @param transformer
	 * @return The created SwipeBack instance.
	 */
	public static SwipeBack attach(Activity activity, Type type, Position position, SwipeBackTransformer transformer) {
		return attach(activity, type, position, DRAG_WINDOW, transformer);
	}



	/**
	 * Attaches the SwipeBack to the Activity
	 * 
	 * @param activity
	 * @param type
	 * @param position
	 * @param dragMode
	 *            The dragMode which is {@link #DRAG_CONTENT} or
	 *            {@link #DRAG_WINDOW}
	 * @return The created SwipeBack instance
	 */
	public static SwipeBack attach(Activity activity, Type type,
			Position position, int dragMode) {
		return attach(activity, type, position, dragMode,
				new DefaultSwipeBackTransformer());
	}

	/**
	 * Attaches the SwipeBack to the Activity
	 * 
	 * @param activity
	 * @param type
	 * @param position
	 * @return The created SwipeBack instance
	 */
	public static SwipeBack attach(Activity activity, Type type,
			Position position) {
		return attach(activity, type, position, DRAG_WINDOW);
	}

	/**
	 * Attaches the SwipeBack to the Activity.
	 * 
	 * @param activity
	 *            The activity the swipe back will be attached to.
	 * @param type
	 *            The {@link SwipeBack.Type} of the drawer.
	 * @param position
	 *            Where to position the swipe back.
	 * @param dragMode
	 *            The drag mode of the drawer. Can be either
	 *            {@link SwipeBack#DRAG_CONTENT} or
	 *            {@link SwipeBack#DRAG_WINDOW}.
	 * @return The created SwipeBack instance.
	 */
	public static SwipeBack attach(Activity activity, Type type, Position position, int dragMode, SwipeBackTransformer transformer) {

		SwipeBack swipeBack = createSwipeBack(activity, dragMode, position, type, transformer);
		swipeBack.setId(R.id.sb__swipeBack);


		switch (dragMode) {
		case SwipeBack.DRAG_CONTENT:
			attachToContent(activity, swipeBack);
			break;

		case SwipeBack.DRAG_WINDOW:
			attachToDecor(activity, swipeBack);
			break;

		default:
			throw new RuntimeException("Unknown drag mode: " + dragMode);
		}


		return swipeBack;
	}

	/**
	 * Constructs the appropriate SwipeBack based on the position.
	 */
	private static SwipeBack createSwipeBack(Activity activity, int dragMode, Position position, Type type, SwipeBackTransformer transformer) {

		SwipeBack drawerHelper;

		if (type == Type.OVERLAY) {
			drawerHelper = new OverlaySwipeBack(activity, dragMode);


		} else {
			drawerHelper = new SlidingSwipeBack(activity, dragMode);

		}


		final SwipeBack drawer = drawerHelper;

		drawer.mDragMode = dragMode;
		drawer.setPosition(position);
		drawer.mSwipeBackTransformer = transformer;


		drawer.initSwipeListener();

		return drawer;
	}

	/**
	 * Determines if the activity has been destroyed or finished. This is useful
	 * to dertermine if a
	 * 
	 * @return
	 */
	@SuppressLint("NewApi")
	protected boolean isActivitiyDestroyed() {

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			return mActivity.isFinishing() || mActivity.isDestroyed();
		} else {
			return mActivity.isFinishing();
		}

	}

	private void initSwipeListener() {
		mOnStateChangeListener = new OnStateChangeListener() {

			@Override
			public void onStateChanged(int oldState, int newState) {

				if (!isActivitiyDestroyed()) {

					if (mSwipeBackTransformer != null) {

						if (STATE_OPEN == newState){
							mSwipeBackTransformer.onSwipeBackCompleted(
									SwipeBack.this, mActivity);
						} else if (STATE_CLOSED == newState){
							mSwipeBackTransformer.onSwipeBackReseted(
									SwipeBack.this, mActivity);
						}

					} else {
						Log.w(TAG, "Internal state changed, but no "
								+ SwipeBackTransformer.class.getSimpleName()
								+ " is registered");
					}

					// Inform additional listener
					if (mAdditionalOnStateChangeListener != null) {
						mAdditionalOnStateChangeListener.onStateChanged(
								oldState, newState);
					}
				}

			}

			@Override
			public void onSlide(float openRatio, int offsetPixels) {

				if (!isActivitiyDestroyed()) {
					if (mSwipeBackTransformer != null) {
						mSwipeBackTransformer.onSwiping(SwipeBack.this,
								openRatio, offsetPixels);
					} else {
						Log.w(TAG,
								"Swiping, but no "
										+ SwipeBackTransformer.class.getSimpleName()
										+ " is registered");
					}

					if (mAdditionalOnStateChangeListener != null) {
						mAdditionalOnStateChangeListener.onSlide(openRatio,
								offsetPixels);

					}
				}
			}
		};
	}

	/**
	 * Attaches the swipe back to the content view.
	 */
	private static void attachToContent(Activity activity, SwipeBack swipeBack) {
		/**
		 * Do not call mActivity#setContentView.
		 * E.g. if using with a ListActivity, Activity#setContentView is overridden and dispatched to
		 * SwipeBack#setContentView, which then again would call Activity#setContentView.
		 */
		ViewGroup content = (ViewGroup) activity
				.findViewById(android.R.id.content);
		content.removeAllViews();
		content.addView(swipeBack, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
	}

	/**
	 * Attaches the swipe back drawer to the window.
	 */
	private static void attachToDecor(Activity activity, SwipeBack swipeBack) {
		ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
		ViewGroup decorChild = (ViewGroup) decorView.getChildAt(0);

		decorView.removeAllViews();
		decorView.addView(swipeBack, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

		swipeBack.mContentContainer.addView(decorChild, decorChild.getLayoutParams());
	}

	SwipeBack(Activity activity, int dragMode) {
		this(activity);

		mActivity = activity;
		mDragMode = dragMode;
	}

	public SwipeBack(Context context) {
		this(context, null);
	}

	public SwipeBack(Context context, AttributeSet attrs) {
		this(context, attrs, R.attr.swipeBackStyle);
	}

	public SwipeBack(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs, defStyle);
	}

	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	protected void init(Context context, AttributeSet attrs, int defStyle) {
		setWillNotDraw(false);
		setFocusable(false);

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SwipeBack, R.attr.swipeBackStyle,
				R.style.Widget_SwipeBack);

		final Drawable contentBackground = a.getDrawable(R.styleable.SwipeBack_sbContentBackground);
		final Drawable swipeBackBackground = a.getDrawable(R.styleable.SwipeBack_sbSwipeBackBackground);

		mSwipeBackViewSize = a.getDimensionPixelSize(R.styleable.SwipeBack_sbSwipeBackSize, dpToPx(DEFAULT_SIZE));

		mDividerEnabled = a.getBoolean(R.styleable.SwipeBack_sbDividerEnabled,
				false);

		mDividerDrawable = a.getDrawable(R.styleable.SwipeBack_sbDivider);

		if (mDividerDrawable == null) {
			mDividerAsShadowColor = a.getColor(R.styleable.SwipeBack_sbDividerAsShadowColor, 0xFF000000);
		} else {
			mCustomDivider = true;
		}

		mDividerSize = a.getDimensionPixelSize(R.styleable.SwipeBack_sbDividerSize,
				dpToPx(DEFAULT_DIVIDER_SIZE_DP));

		mTouchBezelSize = a.getDimensionPixelSize(R.styleable.SwipeBack_sbBezelSize,
				dpToPx(DEFAULT_DRAG_BEZEL_DP));


		mMaxAnimationDuration = a.getInt(R.styleable.SwipeBack_sbMaxAnimationDuration, DEFAULT_ANIMATION_DURATION);



		mDrawOverlay = a.getBoolean(R.styleable.SwipeBack_sbDrawOverlay, false);

		final int position = a.getInt(R.styleable.SwipeBack_sbSwipeBackPosition, 0);
		setPosition(Position.fromValue(position));



		a.recycle();

		mSwipeBackOverlay = new ColorDrawable(0xFF000000);

		mSwipeBackContainer = new NoClickThroughFrameLayout(context);
		mSwipeBackContainer.setId(R.id.sb__swipeBackContainer);

		mContentContainer = new NoClickThroughFrameLayout(context);
		mContentContainer.setId(R.id.sb__content);

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
			mContentContainer.setBackgroundDrawable(contentBackground);
			mSwipeBackContainer.setBackgroundDrawable(swipeBackBackground);
		} else {
			mContentContainer.setBackground(contentBackground);
			mSwipeBackContainer.setBackground(swipeBackBackground);
		}

		initSwipeListener();

	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		View swipeBackView = findViewById(R.id.sbSwipeBackView);
		if (swipeBackView != null) {
			removeView(swipeBackView);
			setSwipeBackView(swipeBackView);
		}

		View content = findViewById(R.id.sbContent);
		if (content != null) {
			removeView(content);
			setContentView(content);
		}

		if (getChildCount() > 2) {
			throw new IllegalStateException(
					"swipe back and content view added in xml must have id's @id/sbSwipeBackView and @id/sbContent");
		}
	}

	public int dpToPx(int dp) {
		return (int) (getResources().getDisplayMetrics().density * dp + 0.5f);
	}

	protected boolean isViewDescendant(View v) {
		ViewParent parent = v.getParent();
		while (parent != null) {
			if (parent == this) {
				return true;
			}

			parent = parent.getParent();
		}

		return false;
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		getViewTreeObserver().addOnScrollChangedListener(mScrollListener);
	}

	@Override
	protected void onDetachedFromWindow() {
		Log.d(TAG, "detach from window");
		getViewTreeObserver().removeOnScrollChangedListener(mScrollListener);
		super.onDetachedFromWindow();
	}


	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
		final int offsetPixels = (int) mOffsetPixels;

		if (mDrawOverlay && offsetPixels != 0) {
			drawOverlay(canvas);
		}
		if (mDividerEnabled && (offsetPixels != 0 || mIsStatic)) {
			drawDivider(canvas);
		}

	}

	protected abstract void drawOverlay(Canvas canvas);

	private void drawDivider(Canvas canvas) {
		// Can't pass the position to the constructor, so wait with loading the drawable until the divider is
		// actually drawn.
		if (mDividerDrawable == null) {
			setDividerAsShadowColor(mDividerAsShadowColor);
		}

		updateDividerRect();
		mDividerDrawable.setBounds(mDividerRect);
		mDividerDrawable.draw(canvas);
	}


	protected void updateDividerRect() {
		// This updates the rect for the static and sliding drawer. The overlay drawer has its own implementation.
		switch (getPosition()) {
		case LEFT:
			mDividerRect.top = 0;
			mDividerRect.bottom = getHeight();
			mDividerRect.right = ViewHelper.getLeft(mContentContainer);
			mDividerRect.left = mDividerRect.right - mDividerSize;
			break;

		case TOP:
			mDividerRect.left = 0;
			mDividerRect.right = getWidth();
			mDividerRect.bottom = ViewHelper.getTop(mContentContainer);
			mDividerRect.top = mDividerRect.bottom - mDividerSize;
			break;

		case RIGHT:
			mDividerRect.top = 0;
			mDividerRect.bottom = getHeight();
			mDividerRect.left = ViewHelper.getRight(mContentContainer);
			mDividerRect.right = mDividerRect.left + mDividerSize;
			break;

		case BOTTOM:
			mDividerRect.left = 0;
			mDividerRect.right = getWidth();
			mDividerRect.top = ViewHelper.getBottom(mContentContainer);
			mDividerRect.bottom = mDividerRect.top + mDividerSize;
			break;
		}
	}



	private void setPosition(Position position) {
		mPosition = position;
		mResolvedPosition = getPosition();
	}

	/**
	 * Disable or enable Touch. You can use this method to disable swipe back
	 * with a swipe gesture.
	 * 
	 * @param enabled
	 */
	public void setTouchEnabled(boolean enabled) {

		if (enabled) {
			setTouchMode(mPreviousTouchMode);
		} else {
			mPreviousTouchMode = getTouchMode();
			setTouchMode(TOUCH_MODE_NONE);
		}

	}

	protected Position getPosition() {
		final int layoutDirection = ViewHelper.getLayoutDirection(this);

		switch (mPosition) {
		case START:
			if (layoutDirection == LAYOUT_DIRECTION_RTL) {
				return Position.RIGHT;
			} else {
				return Position.LEFT;
			}

		case END:
			if (layoutDirection == LAYOUT_DIRECTION_RTL) {
				return Position.LEFT;
			} else {
				return Position.RIGHT;
			}
		}

		return mPosition;
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	@Override
	public void onRtlPropertiesChanged(int layoutDirection) {
		super.onRtlPropertiesChanged(layoutDirection);

		if (!mCustomDivider) {
			setDividerAsShadowColor(mDividerAsShadowColor);
		}

		if (getPosition() != mResolvedPosition) {
			mResolvedPosition = getPosition();
			setOffsetPixels(mOffsetPixels * -1);
		}



		requestLayout();
		invalidate();
	}

	/**
	 * Sets the number of pixels the content should be offset.
	 *
	 * @param offsetPixels The number of pixels to offset the content by.
	 */
	protected void setOffsetPixels(float offsetPixels) {
		final int oldOffset = (int) mOffsetPixels;
		final int newOffset = (int) offsetPixels;

		mOffsetPixels = offsetPixels;

		if (newOffset != oldOffset) {
			onOffsetPixelsChanged(newOffset);
			mSwipeBackViewVisible = newOffset != 0;

			// Notify any attached listeners of the current open ratio
			final float openRatio = ((float) Math.abs(newOffset)) / mSwipeBackViewSize;
			dispatchOnDrawerSlide(openRatio, newOffset);
		}
	}

	/**
	 * Called when the number of pixels the content should be offset by has changed.
	 *
	 * @param offsetPixels The number of pixels to offset the content by.
	 */
	protected abstract void onOffsetPixelsChanged(int offsetPixels);

	/**
	 * Toggles the swipe back open and close with animation.
	 */
	public SwipeBack toggle() {
		return toggle(true);
	}

	/**
	 * Toggles the swipe back open and close.
	 * 
	 * @param animate
	 *            Whether open/close should be animated.
	 */
	public abstract SwipeBack toggle(boolean animate);

	/**
	 * Animates the swipe back open.
	 */
	public SwipeBack open() {
		return open(true);
	}

	/**
	 * Opens the swipe back.
	 * 
	 * @param animate
	 *            Whether open/close should be animated.
	 */
	public abstract SwipeBack open(boolean animate);

	/**
	 * Set the background color of the swipe back view container (The container
	 * where the swipe back view is inflated into)
	 * 
	 * @param color
	 *            The color (not resource id)
	 * @return
	 */
	public SwipeBack setSwipeBackContainerBackgroundColor(int color) {
		mSwipeBackContainer.setBackgroundColor(color);
		return this;
	}


	/**
	 * Set the background of the wipe swipe back view container (The container
	 * where the swipe back view is inflated into)
	 * 
	 * @param d
	 * @return
	 */
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	public SwipeBack setSwipeBackContainerBackgroundDrawable(Drawable d) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
			mSwipeBackContainer.setBackgroundDrawable(d);
		} else {
			mSwipeBackContainer.setBackground(d);
		}

		return this;
	}

	/**
	 * Set the background of the wipe swipe back view container (The container
	 * where the swipe back view is inflated into)
	 * 
	 * @param resId
	 *            The resource id of the drawable. Will interanlly call
	 *            getResources().getDrawable(resId)
	 * @return
	 */
	public SwipeBack setSwipeBackContainerBackgroundDrawable(int resId) {
		return setSwipeBackContainerBackgroundDrawable(getResources()
				.getDrawable(resId));
	}


	/**
	 * Animates the swipe back closed.
	 */
	public SwipeBack close() {
		return close(true);
	}

	/**
	 * Closes the swipe back.
	 *
	 * @param animate Whether open/close should be animated.
	 */
	public abstract SwipeBack close(boolean animate);

	/**
	 * Indicates whether the swipe back is currently visible.
	 * 
	 * @return True if the swipe back is open, false otherwise.
	 */
	public abstract boolean isVisible();

	/**
	 * Set the size of the swipe back when open.
	 * 
	 * @param size
	 *            The size of the swipe back.
	 */
	public abstract SwipeBack setSize(int size);

	/**
	 * Returns the size of the swipe back.
	 * 
	 * @return The size of the swipe back.
	 */
	public int getSize() {
		return mSwipeBackViewSize;
	}


	/**
	 * Scroll listener that checks whether the active view has moved before the drawer is invalidated.
	 */
	private final ViewTreeObserver.OnScrollChangedListener mScrollListener = new ViewTreeObserver.OnScrollChangedListener() {
		@Override
		public void onScrollChanged() {
			if (mActiveView != null && isViewDescendant(mActiveView)) {
				mActiveView.getDrawingRect(mTempRect);
				offsetDescendantRectToMyCoords(mActiveView, mTempRect);
				if (mTempRect.left != mActiveRect.left || mTempRect.top != mActiveRect.top
						|| mTempRect.right != mActiveRect.right || mTempRect.bottom != mActiveRect.bottom) {
					invalidate();
				}
			}
		}
	};



	/**
	 * Compute the touch area based on the touch mode.
	 */
	protected void updateTouchAreaSize() {
		if (mTouchMode == TOUCH_MODE_BEZEL) {
			mTouchSize = mTouchBezelSize;
		} else if (mTouchMode == TOUCH_MODE_FULLSCREEN) {
			mTouchSize = getMeasuredWidth();
		} else {
			mTouchSize = 0;
		}
	}


	/**
	 * Enables or disables offsetting the swipe back when dragging the drawer.
	 * 
	 * @param offsetEnabled
	 *            True to offset the swipe back, false otherwise.
	 */
	public abstract void setOffsetSwipeBackViewEnabled(boolean offsetEnabled);

	/**
	 * Indicates whether the swipe back is being offset when dragging the
	 * drawer.
	 * 
	 * @return True if the swipe back is being offset, false otherwise.
	 */
	public abstract boolean getOffsetSwipeBackEnabled();

	/**
	 * Get the current state of the drawer.
	 *
	 * @return The state of the drawer.
	 */
	public int getState() {
		return mDrawerState;
	}

	/**
	 * Register a callback that will be invoked when the drawer is about to intercept touch events.
	 *
	 * @param listener The callback that will be invoked.
	 */
	public void setOnInterceptMoveEventListener(OnInterceptMoveEventListener listener) {
		mOnInterceptMoveEventListener = listener;
	}

	/**
	 * Defines whether the drop shadow is enabled.
	 *
	 * @param enabled Whether the drop shadow is enabled.
	 */
	public SwipeBack setDividerEnabled(boolean enabled) {
		mDividerEnabled = enabled;
		invalidate();

		return this;
	}

	protected GradientDrawable.Orientation getDividerOrientation() {
		// Gets the orientation for the static and sliding drawer. The overlay drawer provides its own implementation.
		switch (getPosition()) {
		case TOP:
			return GradientDrawable.Orientation.BOTTOM_TOP;

		case RIGHT:
			return GradientDrawable.Orientation.LEFT_RIGHT;

		case BOTTOM:
			return GradientDrawable.Orientation.TOP_BOTTOM;

		default:
			return GradientDrawable.Orientation.RIGHT_LEFT;
		}
	}

	/**
	 * Sets the color of the divider, if you have set the option to use a shadow
	 * gradient as divider
	 * 
	 * You must enable the divider by calling
	 * {@link #setDividerEnabled(boolean)}
	 * 
	 * @param color
	 *            The color of the divider shadow.
	 */
	public SwipeBack setDividerAsShadowColor(int color) {
		GradientDrawable.Orientation orientation = getDividerOrientation();

		final int endColor = color & 0x00FFFFFF;
		GradientDrawable gradient = new GradientDrawable(orientation,
				new int[] {
				color,
				endColor,
		});

		setDivider(gradient);

		return this;
	}

	/**
	 * Sets the drawable of the divider. You must enable the divider by calling
	 * {@link #setDividerEnabled(boolean)}
	 * 
	 * @param drawable
	 *            The drawable of the divider.
	 */
	public SwipeBack setDivider(Drawable drawable) {
		mDividerDrawable = drawable;
		mCustomDivider = drawable != null;
		invalidate();
		return this;
	}

	/**
	 * Sets the drawable resource id of the divider . You must enable the
	 * divider by calling {@link #setDividerEnabled(boolean)}
	 * 
	 * @param resId
	 *            The resource identifier of the the drawable.
	 * 
	 */
	public SwipeBack setDivider(int resId) {
		return setDivider(getResources().getDrawable(resId));
	}

	/**
	 * Returns the drawable of the divider.
	 */
	public Drawable getDivider() {
		return mDividerDrawable;
	}

	/**
	 * Sets the size (width) of the divider. The value is a dp value
	 * 
	 * @param size
	 *            The size (width) of the divider in dp (device independent
	 *            pixel = dp = dip).
	 * @see #setDividerSizeInPixel(int)
	 */
	public SwipeBack setDividerSize(int dp) {
		mDividerSize = dpToPx(dp);
		invalidate();

		return this;
	}

	/**
	 * Sets the size (width) of the divider. The value is in pixel.
	 * 
	 * @param pixel
	 *            The size (widht) of the divider in pixels
	 * @return
	 * @see #setDividerSize(int)
	 */
	public SwipeBack setDividerSizeInPixel(int pixel) {
		mDividerSize = pixel;
		invalidate();
		return this;
	}

	/**
	 * Draw the divider as solid color (using {@link ColorDrawable}).
	 * 
	 * You must enable the divider by calling
	 * {@link #setDividerEnabled(boolean)}
	 * 
	 * @param color
	 * @return
	 */
	public SwipeBack setDividerAsSolidColor(int color) {

		setDivider(new ColorDrawable(color));
		return this;
	}

	/**
	 * Animates the swipe back view slightly open until the user opens it completely.
	 */
	public abstract SwipeBack peekSwipeBack();

	/**
	 * Animates the swipe back view slightly open. If delay is larger than 0, this happens until the user opens the drawer.
	 *
	 * @param delay The delay (in milliseconds) between each run of the animation. If 0, this animation is only run
	 *              once.
	 */
	public abstract SwipeBack peekSwipeBack(long delay);

	/**
	 * Animates the swipe back view slightly open. If delay is larger than 0, this happens until the user opens the drawer.
	 *
	 * @param startDelay The delay (in milliseconds) until the animation is first run.
	 * @param delay      The delay (in milliseconds) between each run of the animation. If 0, this animation is only run
	 *                   once.
	 */
	public abstract SwipeBack peekSwipeBack(long startDelay, long delay);

	/**
	 * Enables or disables the user of {@link android.view.View#LAYER_TYPE_HARDWARE} when animations views.
	 *
	 * @param enabled Whether hardware layers are enabled.
	 */
	public abstract SwipeBack setHardwareLayerEnabled(boolean enabled);

	/**
	 * Sets the maximum duration of open/close animations.
	 *
	 * @param duration The maximum duration in milliseconds.
	 */
	public SwipeBack setMaxAnimationDuration(int duration) {

		mMaxAnimationDuration = duration;
		return this;
	}

	/**
	 * Sets whether an overlay should be drawn when sliding the swipe back view.
	 *
	 * @param drawOverlay Whether an overlay should be drawn when sliding the drawer.
	 */
	public SwipeBack setDrawOverlay(boolean drawOverlay) {
		mDrawOverlay = drawOverlay;
		return this;
	}

	/**
	 * Gets whether an overlay is drawn when sliding the swipe back view.
	 *
	 * @return Whether an overlay is drawn when sliding the drawer.
	 */
	public boolean getDrawOverlay() {
		return mDrawOverlay;
	}




	/**
	 * Returns the ViewGroup used as a parent for the swipe back view.
	 * 
	 * @return The swipe back view's parent.
	 */
	public ViewGroup getSwipeBackContainer() {
		return mSwipeBackContainer;
	}

	/**
	 * Returns the ViewGroup used as a parent for the content view.
	 *
	 * @return The content view's parent.
	 */
	public ViewGroup getContentContainer() {
		if (mDragMode == DRAG_CONTENT) {
			return mContentContainer;
		} else {
			return (ViewGroup) findViewById(android.R.id.content);
		}
	}

	/**
	 * Set the swipe back view from a layout resource.
	 * 
	 * @param layoutResId
	 *            Resource ID to be inflated.
	 */
	public SwipeBack setSwipeBackView(int layoutResId) {
		mSwipeBackContainer.removeAllViews();
		mSwipeBackView = LayoutInflater.from(getContext()).inflate(layoutResId,
				mSwipeBackContainer, false);
		mSwipeBackContainer.addView(mSwipeBackView);

		notifySwipeBackViewCreated(mSwipeBackView);

		return this;
	}

	/**
	 * Set the swipe back view to an explicit view.
	 * 
	 * @param view
	 *            The swipe back view.
	 */
	public SwipeBack setSwipeBackView(View view) {
		setSwipeBackView(view, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		return this;
	}

	/**
	 * Set the swipe back view to an explicit view.
	 * 
	 * @param view
	 *            The swipe back view.
	 * @param params
	 *            Layout parameters for the view.
	 */
	public SwipeBack setSwipeBackView(View view, LayoutParams params) {
		mSwipeBackView = view;
		mSwipeBackContainer.removeAllViews();
		mSwipeBackContainer.addView(view, params);

		notifySwipeBackViewCreated(mSwipeBackView);

		return this;
	}

	/**
	 * Notify the {@link SwipeBackTransformer} that the swipe back has been
	 * created (inflated and ready to use)
	 * 
	 * @param view
	 */
	private void notifySwipeBackViewCreated(View view) {

		if (mSwipeBackTransformer != null) {
			mSwipeBackTransformer.onSwipeBackViewCreated(this, mActivity, view);
		}

	}

	/**
	 * Returns the swipe back view.
	 * 
	 * @return The swipe back view.
	 */
	public View getSwipeBackView() {
		return mSwipeBackView;
	}

	/**
	 * Set the content from a layout resource.
	 *
	 * @param layoutResId Resource ID to be inflated.
	 */
	public SwipeBack setContentView(int layoutResId) {
		switch (mDragMode) {
		case SwipeBack.DRAG_CONTENT:
			mContentContainer.removeAllViews();
			LayoutInflater.from(getContext()).inflate(layoutResId, mContentContainer, true);
			break;

		case SwipeBack.DRAG_WINDOW:
			mActivity.setContentView(layoutResId);
			break;
		}

		return this;
	}

	/**
	 * Set the content to an explicit view.
	 *
	 * @param view The desired content to display.
	 */
	public SwipeBack setContentView(View view) {
		setContentView(view, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		return this;
	}

	/**
	 * Set the content to an explicit view.
	 *
	 * @param view   The desired content to display.
	 * @param params Layout parameters for the view.
	 */
	public SwipeBack setContentView(View view, LayoutParams params) {
		switch (mDragMode) {
		case SwipeBack.DRAG_CONTENT:
			mContentContainer.removeAllViews();
			mContentContainer.addView(view, params);
			break;

		case SwipeBack.DRAG_WINDOW:

			// mActivity can be null if inflated from xml, so retrieve activity
			// if (mActivity == null) {
			// mActivity = (Activity) getContext();
			// }

			mActivity.setContentView(view, params);
			break;
		}

		return this;
	}

	protected SwipeBack setDrawerState(int state) {
		if (state != mDrawerState) {
			final int oldState = mDrawerState;
			mDrawerState = state;
			if (mOnStateChangeListener != null) {
				mOnStateChangeListener.onStateChanged(oldState, state);
			}
			if (DEBUG) {
				logDrawerState(state);
			}
		}

		return this;
	}

	protected SwipeBack logDrawerState(int state) {
		switch (state) {
		case STATE_CLOSED:
			Log.d(TAG, "[DrawerState] STATE_CLOSED");
			break;

		case STATE_CLOSING:
			Log.d(TAG, "[DrawerState] STATE_CLOSING");
			break;

		case STATE_DRAGGING:
			Log.d(TAG, "[DrawerState] STATE_DRAGGING");
			break;

		case STATE_OPENING:
			Log.d(TAG, "[DrawerState] STATE_OPENING");
			break;

		case STATE_OPEN:
			Log.d(TAG, "[DrawerState] STATE_OPEN");
			break;

		default:
			Log.d(TAG, "[DrawerState] Unknown: " + state);
		}

		return this;
	}

	/**
	 * Returns the touch mode.
	 */
	public abstract int getTouchMode();

	/**
	 * Sets the drawer touch mode. Possible values are {@link #TOUCH_MODE_NONE}, {@link #TOUCH_MODE_BEZEL} or
	 * {@link #TOUCH_MODE_FULLSCREEN}.
	 *
	 * @param mode The touch mode.
	 */
	public abstract SwipeBack setTouchMode(int mode);

	/**
	 * Sets the size of the touch bezel.
	 *
	 * @param size The touch bezel size in px.
	 */
	public abstract SwipeBack setTouchBezelSize(int size);

	/**
	 * Returns the size of the touch bezel in px.
	 */
	public abstract int getTouchBezelSize();

	public void disable() {

	}

	/**
	 * Set the {@link SwipeBackTransformer}
	 * 
	 * @param transformer
	 */
	public SwipeBack setSwipeBackTransformer(SwipeBackTransformer transformer) {
		mSwipeBackTransformer = transformer;
		if (mSwipeBackView != null) {
			notifySwipeBackViewCreated(mSwipeBackView);
		}
		return this;
	}

	/**
	 * Get the {@link SwipeBackTransformer}
	 * 
	 * @return
	 */
	public SwipeBackTransformer getSwipeBackTransformer() {
		return mSwipeBackTransformer;
	}

	/**
	 * An additional {@link OnStateChangeListener} which will be plugged in to
	 * the internal delegate to allow to observe the internal state from outside
	 * 
	 * @param listener
	 *            The {@link OnStateChangeListener} listener
	 * @return
	 */
	public SwipeBack setOnStateChangeListener(OnStateChangeListener listener) {
		this.mAdditionalOnStateChangeListener = listener;
		return this;
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	@Override
	public void postOnAnimation(Runnable action) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			super.postOnAnimation(action);
		} else {
			postDelayed(action, ANIMATION_DELAY);
		}
	}



	@Override
	protected boolean fitSystemWindows(Rect insets) {
		if (mDragMode == DRAG_WINDOW && mPosition != Position.BOTTOM) {
			mSwipeBackContainer.setPadding(0, insets.top, 0, 0);
		}
		return super.fitSystemWindows(insets);
	}

	protected void dispatchOnDrawerSlide(float openRatio, int offsetPixels) {
		if (mOnStateChangeListener != null) {
			mOnStateChangeListener.onSlide(openRatio, offsetPixels);
		}
	}

	/**
	 * Saves the state of the drawer.
	 *
	 * @return Returns a Parcelable containing the drawer state.
	 */
	public final Parcelable saveState() {
		if (mState == null) {
			mState = new Bundle();
		}
		saveState(mState);
		return mState;
	}

	void saveState(Bundle state) {
		// State saving isn't required for subclasses.
	}


	/**
	 * Restores the state of the drawer.
	 *
	 * @param in A parcelable containing the drawer state.
	 */
	public void restoreState(Parcelable in) {
		mState = (Bundle) in;
	}

	@Override
	protected Parcelable onSaveInstanceState() {

		return super.onSaveInstanceState();

		// Parcelable superState = super.onSaveInstanceState();
		// SavedState state = new SavedState(superState);
		//
		// if (mState == null) {
		// mState = new Bundle();
		// }
		// saveState(mState);
		//
		// state.mState = mState;
		// return state;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		SavedState savedState = (SavedState) state;
		super.onRestoreInstanceState(savedState.getSuperState());

		restoreState(savedState.mState);
	}

	static class SavedState extends BaseSavedState {

		Bundle mState;

		public SavedState(Parcelable superState) {
			super(superState);
		}

		public SavedState(Parcel in) {
			super(in);
			mState = in.readBundle();
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			super.writeToParcel(dest, flags);
			dest.writeBundle(mState);
		}

		public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
			@Override
			public SavedState createFromParcel(Parcel in) {
				return new SavedState(in);
			}

			@Override
			public SavedState[] newArray(int size) {
				return new SavedState[size];
			}
		};
	}
}
