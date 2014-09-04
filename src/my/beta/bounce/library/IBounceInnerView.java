package my.beta.bounce.library;

import my.beta.bounce.library.BounceLayout.Orientation;

public interface IBounceInnerView {
	/**
	 * 是否可以进行下拉操作了
	 * @return
	 */
	public boolean isReadyForPullStart();

	/**
	 * 是否可以进行上拉操作了
	 * @return
	 */
	public boolean isReadyForPullEnd();
	
	/**
	 * 因为布局结构是BounceLayout->...->IBounceInnerView，
	 * 所以IBounceInnerView直接通过getParent可能得不到
	 * BounceLayout，所以通过该方法直接设置，该方法会在
	 * BounceLayout中得到调用。
	 * @param parent 如果在我们的IBounceLayout中需要用到
	 * BounceLayout，就需要保存这个parent值。
	 */
	public void setBounceParent(BounceLayout parent);
	
	public Orientation getPullOrientation();
}
