package my.beta.bounce.library;

public interface IBounceInnerView {
	public boolean isReadyForPullStart();

	public boolean isReadyForPullEnd();
	
	public void setBounceParent(BounceLayout parent);
}
