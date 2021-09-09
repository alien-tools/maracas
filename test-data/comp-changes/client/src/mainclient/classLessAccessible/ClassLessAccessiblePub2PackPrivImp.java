package mainclient.classLessAccessible;

import main.classLessAccessible.IClassLessAccessiblePub2PackPriv;

public class ClassLessAccessiblePub2PackPrivImp implements IClassLessAccessiblePub2PackPriv {
	public int accessPublicField() {
		return publicField;
	}
	
	public int accessPublicFieldStatic() {
		return IClassLessAccessiblePub2PackPriv.publicField;
	}
	
	public int invokePublicMethod() {
		return IClassLessAccessiblePub2PackPriv.publicMethod();
	}
}
