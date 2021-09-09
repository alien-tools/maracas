package mainclient.fieldNowStatic;

import main.fieldNowStatic.FieldNowStaticSub;

public class FieldNowStaticExtSub extends FieldNowStaticSub {

	public String fieldNowStaticClientSuperKeyAccess() {
		return super.MODIFIED_FIELD;
	}
	
	public String fieldNowStaticClientNoSuperKeyAccess() {
		return MODIFIED_FIELD;
	}
	
}
