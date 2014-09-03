package my.beta.bounce.library;

import android.content.Context;
import android.widget.Scroller;

public class BounceScroller extends Scroller {
	
	private OnSmoothScrollFinishedListener mFinishListener;
	
	public BounceScroller(Context context) {
		super(context);
	}

	@Override
	public void startScroll(int startX, int startY, int dx, int dy) {
		this.startScroll(startX, startY, dx, dy, null);
	}
	
	@Override
	public void startScroll(int startX, int startY, int dx, int dy, int duration) {
		this.startScroll(startX, startY, dx, dy, duration, null);
	}
	
	public void startScroll(int startX, int startY, int dx, int dy, int duration, OnSmoothScrollFinishedListener listener) {
		super.startScroll(startX, startY, dx, dy, duration);
		this.mFinishListener = listener;
	}
	
	public void startScroll(int startX, int startY, int dx, int dy, OnSmoothScrollFinishedListener listener) {
		super.startScroll(startX, startY, dx, dy);
		this.mFinishListener = listener;
	}
	
	@Override
	public boolean computeScrollOffset() {
		boolean nofinish = super.computeScrollOffset();
		if (!nofinish && mFinishListener != null) {
			mFinishListener.onSmoothScrollFinished();
		}
		return nofinish;
	}
	
	static interface OnSmoothScrollFinishedListener {
		void onSmoothScrollFinished();
	}
}
