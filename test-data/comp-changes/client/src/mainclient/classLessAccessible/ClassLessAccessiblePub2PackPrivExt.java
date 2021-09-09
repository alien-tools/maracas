package mainclient.classLessAccessible;

import main.classLessAccessible.ClassLessAccessiblePub2PackPriv;

public class ClassLessAccessiblePub2PackPrivExt extends ClassLessAccessiblePub2PackPriv {

	public void instantiatePub2PackPriv() {
		ClassLessAccessiblePub2PackPriv c1 = new ClassLessAccessiblePub2PackPriv();
		ClassLessAccessiblePub2PackPriv c2 = new ClassLessAccessiblePub2PackPrivExt();
	}
	
	public int accessPublicField() {
		return super.publicField;
	}
	
	public int invokePublicMethod() {
		return super.publicMethod();
	}
}
