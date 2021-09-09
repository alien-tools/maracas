package mainclient.methodLessAccessible;

import main.methodLessAccessible.MethodLessAccessible;

public class MethodLessAccessibleMI {

	MethodLessAccessible m;
	
	public MethodLessAccessibleMI() {
		m = new MethodLessAccessible();
	}
	
	public int methodLessAccessiblePub2ProClientSimpleAccess() {
		return m.methodLessAccessiblePublic2Protected();
	}
	
	public int methodLessAccessiblePub2PackPrivClientSimpleAccess() {
		return m.methodLessAccessiblePublic2PackPriv();
	}
	
	public int methodLessAccessiblePub2PrivClientSimpleAccess() {
		return m.methodLessAccessiblePublic2Private();
	}
	
}
