package testing_lib.accessModifierClazzConstructorAccessDecrease;

public class AccessModifierClazzConstructorAccessDecrease {

	protected AccessModifierClazzConstructorAccessDecrease() {
		// Public to protected
	}
	
	AccessModifierClazzConstructorAccessDecrease(int a) {
		// Public to non
	}
	
	private AccessModifierClazzConstructorAccessDecrease(int a, int b) {
		// Public to private
	}
	
	AccessModifierClazzConstructorAccessDecrease(int a, int b, int c) {
		// protected to non
	}
	
	private AccessModifierClazzConstructorAccessDecrease(int a, int b, int c, int d) {
		// protected to private
	}
	
	private AccessModifierClazzConstructorAccessDecrease(int a, int b, int c, int d, int e) {
		// non to private
	}
	
}
