package mainclient.classLessAccessible;

import main.classLessAccessible.ClassLessAccessiblePro2Priv;

public class ClassLessAccessiblePro2PrivExt extends ClassLessAccessiblePro2Priv {

	public void instantiatePro2Priv() {
		ClassLessAccessiblePro2PrivInner c = new ClassLessAccessiblePro2PrivExtInner();
	}
	
	public class ClassLessAccessiblePro2PrivExtInner extends ClassLessAccessiblePro2PrivInner {
		
		public int accessPublicField() {
			return super.publicField;
		}
		
		public int invokePublicMethod() {
			return super.publicMethod();
		}
	}
}
