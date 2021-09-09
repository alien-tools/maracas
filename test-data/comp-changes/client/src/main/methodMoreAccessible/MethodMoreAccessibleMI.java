package main.methodMoreAccessible;

public class MethodMoreAccessibleMI {

	private MethodMoreAccessible c;
	
	public MethodMoreAccessibleMI() {
		c = new MethodMoreAccessible();
	}
	
	public void protected2public() {
		c.protected2public();
	}
	
	public void pkgProtected2public() {
		c.pkgProtected2public();
	}
	
	public void pkgProtected2protected() {
		c.pkgProtected2protected();
	}
}
