package my.beta.bounce.library;


import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Scroller;

public class BounceLayout extends LinearLayout {

	private static final int INVALID_COORDINATE = -1;
	
	private IBounceInnerView mChildView;
	
	private boolean mIsBeingDragged;
	
	private float mLastMotionX, mLastMotionY;
	private float mInitialMotionX, mInitialMotionY;
	
	private int mTouchSlop;
	
	private Mode mMode = Mode.getDefault();
	private Mode mCurrentMode;
	
	private Scroller mScroller;
	
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
		mScroller = new Scroller(context);
		mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
		resetCoordinate();
	}
	
	@Override
	public void addView(View child, int index, ViewGroup.LayoutParams params) {
		if (getChildCount() > 0) {
			throw new IllegalArgumentException("only allow one child view!");
		}
		
		super.addView(child, index, params);
		
		try {
			mChildView = (IBounceInnerView) child;
		} catch (ClassCastException e) {
			throw new IllegalArgumentException("child view must implements IBounceInnerView!");
		}
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
		if (mChildView == null) {
			return false;
		}
		
		final int action = ev.getAction();
		switch (action) {
		case MotionEvent.ACTION_MOVE:
			if (isReadyForPull()) {
				float tempX = ev.getX();
				float tempY = ev.getY();
				float distance = tempY - mLastMotionY;
				float absDistance = Math.abs(distance);
				if (absDistance >= mTouchSlop) {
					if (mMode.allowPullFromStart() && distance > 0f && mChildView.isReadyForPullStart()) {
						mLastMotionX = tempX;
						mLastMotionY = tempY;
						mCurrentMode = Mode.PULL_FROM_START;
						mIsBeingDragged = true;
					} else if (mMode.allowPullFromEnd() && distance < 0f && mChildView.isReadyForPullEnd()) {
						mLastMotionX = tempX;
						mLastMotionY = tempY;
						mCurrentMode = Mode.PULL_FROM_END;
						mIsBeingDragged = true;
					}
				}
			}
			break;
		}
		
		return mIsBeingDragged;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		
		if (event.getAction() == MotionEvent.ACTION_DOWN && event.getEdgeFlags() != 0) {
			return false;
		}
		
		final int action = event.getAction();
		switch (action) {
		case MotionEvent.ACTION_MOVE:
			if (mIsBeingDragged) {
				mLastMotionY = event.getY();
				int newScrollValue = 0;
				if (mCurrentMode == Mode.PULL_FROM_START) {
					newScrollValue = Math.round(Math.min(mInitialMotionY - mLastMotionY, 0) / 2);
				} else if (mCurrentMode == Mode.PULL_FROM_END) {
					newScrollValue = Math.round(Math.max(mInitialMotionY - mLastMotionY, 0) / 2);
				}
				scrollTo(0, newScrollValue);
				
				return true;
			}
			break;
			
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			if (mIsBeingDragged) {
				mIsBeingDragged = false;
				int scrollY = getScrollY();
				mScroller.startScroll(0, scrollY, 0, -scrollY);
				invalidate();
			}
			
			break;
		}
		
		return super.onTouchEvent(event);
	}
	
	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			scrollTo(0, mScroller.getCurrY());
			invalidate();
		}
	}
	
	public final void setMode(Mode mode) {
		if (mode != mMode) {
			mMode = mode;
			mCurrentMode = (mMode != Mode.PULL_FROM_BOTH) ? mMode : Mode.PULL_FROM_START;
		}
	}
	
	private boolean isReadyForPull() {
		if (mChildView != null) {
			return mChildView.isReadyForPullEnd() || mChildView.isReadyForPullStart();
		}
		return false;
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
}
