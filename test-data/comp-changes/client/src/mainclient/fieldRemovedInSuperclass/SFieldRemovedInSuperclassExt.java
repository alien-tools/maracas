package mainclient.fieldRemovedInSuperclass;

import main.fieldRemovedInSuperclass.SFieldRemovedInSuperclass;

public class SFieldRemovedInSuperclassExt extends SFieldRemovedInSuperclass {

	public int accessSuperKey() {
		return super.removedField;
	}
	
	public int accessNoSuperKey() {
		return removedField;
	}
}
