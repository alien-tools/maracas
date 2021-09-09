package main.classLessAccessible;

public class ClassLessAccessiblePro2PackPriv {

	protected class ClassLessAccessiblePro2PackPrivInner {
		
		public int publicField;
		private int privateField;
		
		public int publicMethod() {
			return 0;
		}
		
		public int privateMethod() {
			return 0;
		}
	}
}
