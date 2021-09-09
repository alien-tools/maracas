package main.methodLessAccessible;

public class MethodLessAccessibleMI {

	public void clientPublic() {
		MethodLessAccessible c = new MethodLessAccessible();
		int i = c.methodLessAccessiblePublic2PackPriv();
	}
	
	public void clientProtected() {
		MethodLessAccessible c = new MethodLessAccessible();
		int i = c.methodLessAccessibleProtected2PackPriv();
	}
}
