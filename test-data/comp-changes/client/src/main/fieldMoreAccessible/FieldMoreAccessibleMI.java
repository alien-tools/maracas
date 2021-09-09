package main.fieldMoreAccessible;

public class FieldMoreAccessibleMI {

	private FieldMoreAccessible c;

	public FieldMoreAccessibleMI() {
		c = new FieldMoreAccessible();
	}

	public void packageprivate2protected() {
		int v = c.packageprivate2protected;
	}

	public void packageprivate2public() {
		int v = c.packageprivate2public;
	}

	public void protected2public() {
		int v = c.protected2public;
	}

	public void superPackagePrivate2Public() {
		int v = c.superPackagePrivate2Public;
	}

	public void superPackagePrivateToProtected() {
		int v = c.superPackagePrivateToProtected;
	}

	public void superProtected2Public() {
		int v = c.superProtected2Public;
	}
}
