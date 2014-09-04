package my.beta.bounce.library;

import my.beta.bounce.library.BounceLayout.Orientation;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;

public class BounceViewPager extends ViewPager implements IBounceInnerView {

	public BounceViewPager(Context context) {
		super(context);
	}

	public BounceViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public boolean isReadyForPullStart() {
		PagerAdapter adapter = getAdapter();
		if (adapter != null) {
			return getCurrentItem() == 0;
		}
		return true;
	}

	@Override
	public boolean isReadyForPullEnd() {
		PagerAdapter adapter = getAdapter();
		if (adapter != null) {
			return getCurrentItem() == adapter.getCount() - 1;
		}
		return true;
	}

	@Override
	public void setBounceParent(BounceLayout parent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Orientation getPullOrientation() {
		return Orientation.HORIZONTAL;
	}

}
