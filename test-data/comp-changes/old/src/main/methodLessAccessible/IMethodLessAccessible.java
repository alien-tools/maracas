package main.methodLessAccessible;

public interface IMethodLessAccessible {

	public int methodLessAccessiblePublic2PackPriv();
	// This one is public by default
	int methodLessAccessiblePackPriv2Public();
}
