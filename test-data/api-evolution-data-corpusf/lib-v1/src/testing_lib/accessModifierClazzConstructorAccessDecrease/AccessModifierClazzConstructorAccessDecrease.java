package testing_lib.accessModifierClazzConstructorAccessDecrease;

public class AccessModifierClazzConstructorAccessDecrease {

	public AccessModifierClazzConstructorAccessDecrease() {
		// Public to protected
	}
	
	public AccessModifierClazzConstructorAccessDecrease(int a) {
		// Public to non
	}
	
	public AccessModifierClazzConstructorAccessDecrease(int a, int b) {
		// Public to private
	}
	
	protected AccessModifierClazzConstructorAccessDecrease(int a, int b, int c) {
		// protected to non
	}
	
	protected AccessModifierClazzConstructorAccessDecrease(int a, int b, int c, int d) {
		// protected to private
	}
	
	AccessModifierClazzConstructorAccessDecrease(int a, int b, int c, int d, int e) {
		// non to private
	}
	
}
