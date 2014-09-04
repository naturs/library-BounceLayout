package my.beta.bounce.library;


import my.beta.bounce.library.BounceScroller.OnSmoothScrollFinishedListener;
import my.beta.bounce.library.util.Logger;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public class BounceLayout extends LinearLayout {

	private static final String LOG_TAG = "Bonce";
	
	private static final int INVALID_COORDINATE = -1;
	
	private static final float FRACTION = 2.0f;
	
	private IBounceInnerView mBounceView;
	
	private FrameLayout mBounceViewWrapper;
	
	private boolean mIsBeingDragged;
	
	private float mLastMotionX, mLastMotionY;
	private float mInitialMotionX, mInitialMotionY;
	
	private int mTouchSlop;
	
	private Mode mMode = Mode.getDefault();
	private Mode mCurrentMode;
	
	private BounceScroller mScroller;
	
	public BounceLayout(Context context) {
		super(context);
	}

	public BounceLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	
	private void init(Context context) {
		setOrientation(LinearLayout.VERTICAL);
		setGravity(Gravity.CENTER);
		mScroller = new BounceScroller(context);
		mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
		resetCoordinate();
	}
	
	@Override
	public void addView(View child, int index, ViewGroup.LayoutParams params) {
		if (getChildCount() > 0) {
			throw new IllegalArgumentException("only allow one child view!");
		}
		
		if (child instanceof IBounceInnerView) {
			addBounceView(child, index, params);
		} else {
			throw new IllegalArgumentException("child view must implements IBounceInnerView!");
		}
		
	}
	
	/**
	 * 添加bounce view，添加后的结构为LinearLayout->FrameLayout->BounceView
	 * @param child
	 * @param index
	 * @param params
	 */
	private void addBounceView(View child, int index, ViewGroup.LayoutParams params) {
		mBounceViewWrapper = new FrameLayout(getContext());
		mBounceViewWrapper.addView(child, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		super.addView(mBounceViewWrapper, index, params);
		mBounceView = (IBounceInnerView) child;
		mBounceView.setBounceParent(this);
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (isReadyForPull()) {
				requestDisallowInterceptTouchEvent(false);
				mInitialMotionX = mLastMotionX = ev.getX();
				mInitialMotionY = mLastMotionY = ev.getY();
			}
			if (!mScroller.isFinished()) {
				mScroller.abortAnimation();
			}
			mIsBeingDragged = false;
			break;
			
		case MotionEvent.ACTION_MOVE:
			if (isReadyForPull()) {
				if (mInitialMotionX == INVALID_COORDINATE) {
					mInitialMotionX = mLastMotionX = ev.getX();
					mInitialMotionY = mLastMotionY = ev.getY();
					// XXX
					return true;
				}
				requestDisallowInterceptTouchEvent(false);
			}
			break;
			
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			resetCoordinate();
			break;
		}
		
		return super.dispatchTouchEvent(ev);
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (mBounceView == null) {
			return false;
		}
		
		final int action = ev.getAction();
		switch (action) {
		case MotionEvent.ACTION_MOVE:
			if (isReadyForPull()) {
				float tempX = ev.getX();
				float tempY = ev.getY();
				
				float distance, absDistance;
				switch (getPullOrientation()) {
				case HORIZONTAL:
					distance = tempX - mLastMotionX;
					break;
					
				case VERTICAL:
				default:
					distance = tempY - mLastMotionY;
					break;
				}
				absDistance = Math.abs(distance);
				
//				if (absDistance >= mTouchSlop) {
					if (mMode.allowPullFromStart() && distance > 0f && isReadyForPullStart()) {
						mLastMotionX = tempX;
						mLastMotionY = tempY;
						mCurrentMode = Mode.PULL_FROM_START;
						mIsBeingDragged = true;
					} else if (mMode.allowPullFromEnd() && distance < 0f && isReadyForPullEnd()) {
						mLastMotionX = tempX;
						mLastMotionY = tempY;
						mCurrentMode = Mode.PULL_FROM_END;
						mIsBeingDragged = true;
					}
//				}
			}
			break;
		}
		
		Logger.i(LOG_TAG, "intercept touch event: " + mIsBeingDragged);
		
		return mIsBeingDragged;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		
		if (event.getAction() == MotionEvent.ACTION_DOWN && event.getEdgeFlags() != 0) {
			return false;
		}
		
		final int action = event.getAction();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			break;
			
		case MotionEvent.ACTION_MOVE:
			if (mIsBeingDragged) {
				mLastMotionX = event.getX();
				mLastMotionY = event.getY();
				
				final int newScrollValue;
				final float initialMotionValue, lastMotionValue;
				
				switch (getPullOrientation()) {
				case HORIZONTAL:
					initialMotionValue = mInitialMotionX;
					lastMotionValue = mLastMotionX;
					break;
					
				case VERTICAL:
				default:
					initialMotionValue = mInitialMotionY;
					lastMotionValue = mLastMotionY;
					break;
				}
				
				switch (mCurrentMode) {
				case PULL_FROM_END:
					newScrollValue = Math.round(Math.max(initialMotionValue - lastMotionValue, 0) / FRACTION);
					break;
					
				case PULL_FROM_START:
				default:
					newScrollValue = Math.round(Math.min(initialMotionValue - lastMotionValue, 0) / FRACTION);
					break;
				}
				
				doScroll(newScrollValue);
				
				return true;
			}
			break;
			
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			if (mIsBeingDragged) {
				mIsBeingDragged = false;
				smoothScrollTo(0);
				invalidate();
			}
			
			break;
		}
		
		return super.onTouchEvent(event);
	}
	
	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			doScroll(mScroller.getCurrY());
			invalidate();
		}
	}
	
	public final void setMode(Mode mode) {
		if (mode != mMode) {
			mMode = mode;
			mCurrentMode = (mMode != Mode.PULL_FROM_BOTH) ? mMode : Mode.PULL_FROM_START;
		}
	}
	
	protected final void smoothScrollTo(int newScrollValue) {
		smoothScrollTo(newScrollValue, null);
	}
	
	protected final void smoothScrollTo(int newScrollValue, int duration) {
		this.smoothScrollTo(newScrollValue, duration, null);
	}
	
	protected final void smoothScrollTo(int newScrollValue, OnSmoothScrollFinishedListener l) {
		this.smoothScrollTo(newScrollValue, 0, l);
	}
	
	protected final void smoothScrollTo(int newScrollValue, int duration, OnSmoothScrollFinishedListener l) {
		if (mScroller.isFinished()) {
			final int oldScrollValue = getScrollY();
			if (oldScrollValue != newScrollValue) {
				if (duration > 0) {
					mScroller.startScroll(0, oldScrollValue, 0, newScrollValue - oldScrollValue, duration, l);
				} else {
					mScroller.startScroll(0, oldScrollValue, 0, newScrollValue - oldScrollValue, l);
				}
				invalidate();
			}
		} else {
			mScroller.abortAnimation();
			doScroll(newScrollValue);
		}
	}
	
	protected FrameLayout getBounceViewWrapper() {
		return mBounceViewWrapper;
	}
	
	/**
	 * 将empty view添加到当前LinearLayout的布局下
	 * @param emptyView 比如ListView的empty view
	 */
	protected final void setEmptyView(View emptyView) {
		FrameLayout bounceViewWrapper = getBounceViewWrapper();
		if (emptyView != null) {
			emptyView.setClickable(true);
			ViewParent emptyViewParent = emptyView.getParent();
			if (emptyViewParent != null && emptyViewParent instanceof ViewGroup) {
				((ViewGroup) emptyViewParent).removeView(emptyView);
			}
			bounceViewWrapper.addView(emptyView);
		}
	}
	
	private boolean isReadyForPull() {
		return isReadyForPullEnd() || isReadyForPullStart();
	}
	
	private boolean isReadyForPullStart() {
		if (mBounceView != null) {
			return mBounceView.isReadyForPullStart();
		}
		return false;
	}
	
	private boolean isReadyForPullEnd() {
		if (mBounceView != null) {
			return mBounceView.isReadyForPullEnd();
		}
		return false;
	}
	
	private Orientation getPullOrientation() {
		return mBounceView.getPullOrientation();
	}
	
	private int getMaximumPullScroll() {
		int max;
		switch (getPullOrientation()) {
		case HORIZONTAL:
			max = Math.round(getWidth() / FRACTION);
			break;
			
		case VERTICAL:
		default:
			max = Math.round(getHeight() / FRACTION);
			break;
		}
		return max;
	}
	
	private void doScroll(int newScrollValue) {
		final int maximumPullScroll = getMaximumPullScroll();
		newScrollValue = Math.min(Math.max(-maximumPullScroll, newScrollValue), maximumPullScroll);
		
		Logger.d(LOG_TAG, "doScroll -> " + newScrollValue);
		
		switch (getPullOrientation()) {
		case HORIZONTAL:
			scrollTo(newScrollValue, 0);
			break;
			
		case VERTICAL:
		default:
			scrollTo(0, newScrollValue);
			break;
		}
	}
	
	private void resetCoordinate() {
		mInitialMotionX = mInitialMotionY = mLastMotionX = mLastMotionY = INVALID_COORDINATE;
	}
	
	public static enum Mode {
		DISABLE(0x0),
		PULL_FROM_START(0x1),
		PULL_FROM_END(0x2),
		PULL_FROM_BOTH(0x3);
		
		private int value;
		
		private Mode(int value) {
			this.value = value;
		}
		
		public static Mode getDefault() {
			return PULL_FROM_BOTH;
		}
		
		public int getValue() {
			return value;
		}
		
		public boolean allowPullFromStart() {
			return this == PULL_FROM_START || this == PULL_FROM_BOTH;
		}
		
		public boolean allowPullFromEnd() {
			return this == PULL_FROM_END || this == PULL_FROM_BOTH;
		}
	}
	
	public static enum Orientation {
		VERTICAL,
		HORIZONTAL;
	}
	
}
