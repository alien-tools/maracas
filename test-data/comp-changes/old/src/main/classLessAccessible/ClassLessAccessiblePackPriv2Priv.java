package main.classLessAccessible;

public class ClassLessAccessiblePackPriv2Priv {

	class ClassLessAccessiblePackPriv2PrivInner {
		
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
