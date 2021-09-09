package mainclient.fieldRemoved;

import main.fieldRemoved.FieldRemovedSub;

public class FieldRemovedExtSub extends FieldRemovedSub {

	public int fieldRemovedClientExt() {
		return fieldRemoved;
	}
	
	public int fieldRemovedClientSuper() {
		return super.fieldRemoved;
	}
}
