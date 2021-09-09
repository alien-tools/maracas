package mainclient.fieldRemovedInSuperclass;

import main.fieldRemovedInSuperclass.FieldRemovedInSuperclass;

public class FieldRemovedInSuperclassExt extends FieldRemovedInSuperclass {

	public int accessSuperKey() {
		return super.removedField;
	}
	
	public int accessNoSuperKey() {
		return removedField;
	}
}
