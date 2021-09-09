package mainclient.unstablePkg.fieldRemoved;

import main.unstablePkg.fieldRemoved.FieldRemoved;
import main.unstablePkg.fieldRemoved.FieldRemovedSub;

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
