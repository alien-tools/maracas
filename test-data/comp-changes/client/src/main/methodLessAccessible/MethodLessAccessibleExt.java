package main.methodLessAccessible;

public class MethodLessAccessibleExt extends MethodLessAccessible {

	@Override
	public int methodLessAccessiblePublic2PackPriv() {
		return 11;
	}
	
	@Override
	public int methodLessAccessibleProtected2PackPriv() {
		return 12;
	}
}
