package my.beta.bounce.library;

import my.beta.bounce.library.BounceLayout.Orientation;
import my.beta.bounce.library.BounceScroller.OnSmoothScrollFinishedListener;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

@SuppressLint("NewApi")
public class BounceListView extends ListView implements IBounceInnerView {

	private BounceLayout mBounceParent;
	
	public BounceListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD) {
			setOverScrollMode(View.OVER_SCROLL_NEVER);
		}
	}

	@Override
	public boolean isReadyForPullStart() {
		final ListAdapter adapter = getAdapter();
		if (adapter == null || adapter.isEmpty()) {
			return true;
		}
		
		if (getFirstVisiblePosition() <= 0) {
			final View firstVisibleChild = getChildAt(0);
			if (firstVisibleChild != null) {
				return firstVisibleChild.getTop() >= getTop();
			}
		}
		return false;
	}
	
	@Override
	public boolean isReadyForPullEnd() {
		final ListAdapter adapter = getAdapter();
		if (adapter == null || adapter.isEmpty()) {
			return true;
		}
		
		final int lastItemPosition = this.getCount() - 1;
		final int lastVisibleItemPosition = this.getLastVisiblePosition();
		if (lastVisibleItemPosition >= lastItemPosition) {
			final int lastChildIndex = lastVisibleItemPosition - this.getFirstVisiblePosition();
			View lastChild = getChildAt(lastChildIndex);
			if (lastChild != null) {
				return lastChild.getBottom() <= this.getBottom();
			}
		}
		
		return false;
	}
	
	@Override
	public void setBounceParent(BounceLayout parent) {
		mBounceParent = parent;
	}
	
	@Override
	public Orientation getPullOrientation() {
		return Orientation.VERTICAL;
	}
	
	@Override
	public void setEmptyView(View emptyView) {
		mBounceParent.setEmptyView(emptyView);
		super.setEmptyView(emptyView);
	}
	
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	@Override
	protected boolean overScrollBy(int deltaX, int deltaY, int scrollX,
			int scrollY, int scrollRangeX, int scrollRangeY,
			int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
		System.out.println("deltaY:" + deltaY + " , scrollY:" + scrollY + " , scrollRangeY:" + scrollRangeY + " , maxOverScrollY:" + maxOverScrollY + " , isTouchEvent:" + isTouchEvent);

		if (!isTouchEvent) {
			// 这里加个150ms的时间，效果会更好一点
			mBounceParent.smoothScrollTo(deltaY, 150, new OnSmoothScrollFinishedListener() {
				
				@Override
				public void onSmoothScrollFinished() {
					mBounceParent.smoothScrollTo(0);
				}
			});
		}
		return super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX,
				scrollRangeY, maxOverScrollX, maxOverScrollY, isTouchEvent);
	}
	
}
