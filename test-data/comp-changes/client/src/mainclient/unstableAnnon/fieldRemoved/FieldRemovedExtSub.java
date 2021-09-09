package mainclient.unstableAnnon.fieldRemoved;

import main.unstableAnnon.fieldRemoved.FieldRemovedSub;

public class FieldRemovedExtSub extends FieldRemovedSub {

	public int fieldRemovedClientExt() {
		return fieldRemoved;
	}
	
	public int fieldRemovedClientSuper() {
		return super.fieldRemoved;
	}
}
