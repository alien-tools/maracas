package mainclient.unstableAnnon.fieldRemoved;

import main.unstableAnnon.fieldRemoved.FieldRemoved;
import main.unstableAnnon.fieldRemoved.FieldRemovedSub;

public class FieldRemovedFA {

	public int fieldRemovedClient() {
		FieldRemoved fr = new FieldRemoved();
		return fr.fieldRemoved;
	}
	
	public int fieldRemovedClientSub() {
		FieldRemovedSub fr = new FieldRemovedSub();
		return fr.fieldRemoved;
	}
}
