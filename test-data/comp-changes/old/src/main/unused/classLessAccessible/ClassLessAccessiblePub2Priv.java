package main.unused.classLessAccessible;

public class ClassLessAccessiblePub2Priv {

	public class ClassLessAccessiblePub2PrivInner {
		
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
