package mainclient.classLessAccessible;

import main.classLessAccessible.ClassLessAccessiblePub2Priv;

public class ClassLessAccessiblePub2PrivExt extends ClassLessAccessiblePub2Priv {

	public void instantiatePub2Priv() {
		ClassLessAccessiblePub2PrivInner c1 = new ClassLessAccessiblePub2PrivInner();
		ClassLessAccessiblePub2PrivInner c2 = new ClassLessAccessiblePub2PrivExtInner();
	}
	
	public class ClassLessAccessiblePub2PrivExtInner extends ClassLessAccessiblePub2PrivInner {
		
		public int accessPublicField() {
			return super.publicField;
		}
		
		public int invokePublicMethod() {
			return super.publicMethod();
		}
	}
}
