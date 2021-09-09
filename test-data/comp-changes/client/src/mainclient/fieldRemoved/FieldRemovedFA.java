package mainclient.fieldRemoved;

import main.fieldRemoved.FieldRemoved;
import main.fieldRemoved.FieldRemovedSub;

public class FieldRemovedFA {

	public int fieldRemoved() {
		FieldRemoved fr1 = new FieldRemoved();
		int a = fr1.fieldRemoved;
		int b = fr1.fieldStay;
		int c = fr1.staticFieldRemoved;
		int d = fr1.staticFieldStay;

		FieldRemovedSub fr2 = new FieldRemovedSub();
		int e = fr2.fieldRemoved;
		int f = fr2.fieldStay;
		int j = fr2.staticFieldRemoved;
		return fr2.staticFieldStay;
	}

}
