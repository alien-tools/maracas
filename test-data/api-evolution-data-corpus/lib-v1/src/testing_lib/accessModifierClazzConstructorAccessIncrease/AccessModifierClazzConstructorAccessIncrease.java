package testing_lib.accessModifierClazzConstructorAccessIncrease;

public class AccessModifierClazzConstructorAccessIncrease {

	private AccessModifierClazzConstructorAccessIncrease() {
		// private to non
	}
	
	private AccessModifierClazzConstructorAccessIncrease(int a) {
		// private to protected
	}
	
	private AccessModifierClazzConstructorAccessIncrease(int a, int b) {
		// private to public
	}
	
	AccessModifierClazzConstructorAccessIncrease(int a, int b, int c) {
		// non to protected
	}
	
	AccessModifierClazzConstructorAccessIncrease(int a, int b, int c, int d) {
		// non to public 
	}
	
	protected AccessModifierClazzConstructorAccessIncrease(int a, int b, int c, int d, int e) {
		// protected to public
	}
	
}
