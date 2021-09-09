package mainclient.fieldRemovedInSuperclass;

import main.fieldRemovedInSuperclass.FieldRemovedInSuperclass;
import main.fieldRemovedInSuperclass.SFieldRemovedInSuperclass;

public class FieldRemovedInSuperclassFA {

	public int accessSuper() {
		SFieldRemovedInSuperclass s = new SFieldRemovedInSuperclass();
		return s.removedField;
	}
	
	public int accessSub() {
		FieldRemovedInSuperclass s = new FieldRemovedInSuperclass();
		return s.removedField;
	}
}
