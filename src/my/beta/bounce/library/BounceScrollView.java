package my.beta.bounce.library;

import my.beta.bounce.library.BounceLayout.Orientation;
import my.beta.bounce.library.BounceScroller.OnSmoothScrollFinishedListener;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ScrollView;

public class BounceScrollView extends ScrollView implements IBounceInnerView {

	static final String LOG_TAG = "BounceScrollView";
	
	private BounceLayout mBounceParent;
	private int mOverScrollLength;
	private boolean mIsTouchedScroll;
	
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	public BounceScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setOverScrollMode(View.OVER_SCROLL_NEVER);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public boolean isReadyForPullStart() {
		return getScrollY() <= 0;
	}

	@Override
	public boolean isReadyForPullEnd() {
		if (getChildCount() > 0) {
			View scrollViewChild = getChildAt(0);
			if (scrollViewChild != null) {
				return getScrollY() >= getScrollRange();
			}
		}
		return true;
	}
	
	private int getScrollRange() {
		int scrollRange = 0;
		if (getChildCount() > 0) {
			View child = getChildAt(0);
			scrollRange = Math.max(0, child.getHeight() - (getHeight() - getPaddingBottom() - getPaddingTop()));
		}
		return scrollRange;
	}

	@Override
	public void setBounceParent(BounceLayout parent) {
		mBounceParent = parent;
	}

	@Override
	public Orientation getPullOrientation() {
		return Orientation.VERTICAL;
	}

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	@Override
	protected boolean overScrollBy(int deltaX, int deltaY, int scrollX,
			int scrollY, int scrollRangeX, int scrollRangeY,
			int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
		mOverScrollLength = deltaY;
		mIsTouchedScroll = isTouchEvent;
		return super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX,
				scrollRangeY, maxOverScrollX, maxOverScrollY, isTouchEvent);
	}
	
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	@Override
	protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX,
			boolean clampedY) {
		if (!mIsTouchedScroll && clampedY) {
			mBounceParent.smoothScrollTo(mOverScrollLength, 150, new OnSmoothScrollFinishedListener() {
				
				@Override
				public void onSmoothScrollFinished() {
					mBounceParent.smoothScrollTo(0);
				}
			});
		}
		super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
	}
}
