package mainclient.classLessAccessible;

import main.classLessAccessible.ClassLessAccessiblePro2PackPriv;

public class ClassLessAccessiblePro2PackPrivExt extends ClassLessAccessiblePro2PackPriv {

	public void instantiatePro2PackPriv() {
		ClassLessAccessiblePro2PackPrivInner c = new ClassLessAccessiblePro2PackPrivExtInner();
	}
	
	public class ClassLessAccessiblePro2PackPrivExtInner extends ClassLessAccessiblePro2PackPrivInner {
		
		public int accessPublicField() {
			return super.publicField;
		}
		
		public int invokePublicMethod() {
			return super.publicMethod();
		}
	}
}
