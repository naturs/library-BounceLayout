package my.beta.bounce.library;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

public class BounceListView extends ListView implements IBounceInnerView {

	public BounceListView(Context context, AttributeSet attrs) {
		super(context, attrs);
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
	
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	@Override
	protected boolean overScrollBy(int deltaX, int deltaY, int scrollX,
			int scrollY, int scrollRangeX, int scrollRangeY,
			int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
		System.out.println("over:" + maxOverScrollY + "  " + isTouchEvent);
		return super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX,
				scrollRangeY, maxOverScrollX, maxOverScrollY, isTouchEvent);
	}
	
}
