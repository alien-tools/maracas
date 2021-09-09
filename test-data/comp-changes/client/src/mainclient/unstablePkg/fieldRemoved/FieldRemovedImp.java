package mainclient.unstablePkg.fieldRemoved;

import main.unstablePkg.fieldRemoved.IFieldRemoved;

public class FieldRemovedImp implements IFieldRemoved {

	public int fieldRemovedClient() {
		return FIELD_REMOVED;
	}
	
	public int fieldRemovedClientType() {
		return IFieldRemoved.FIELD_REMOVED;
	}
}
