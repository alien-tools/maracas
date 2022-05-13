package testing_lib.accessModifierClazzConstructorAccessIncrease;

public class AccessModifierClazzConstructorAccessIncrease {

	AccessModifierClazzConstructorAccessIncrease() {
		// private to non
	}
	
	protected AccessModifierClazzConstructorAccessIncrease(int a) {
		// private to protected
	}
	
	public AccessModifierClazzConstructorAccessIncrease(int a, int b) {
		// private to public
	}
	
	protected AccessModifierClazzConstructorAccessIncrease(int a, int b, int c) {
		// non to protected
	}
	
	public AccessModifierClazzConstructorAccessIncrease(int a, int b, int c, int d) {
		// non to public 
	}
	
	public AccessModifierClazzConstructorAccessIncrease(int a, int b, int c, int d, int e) {
		// protected to public
	}
	
}
