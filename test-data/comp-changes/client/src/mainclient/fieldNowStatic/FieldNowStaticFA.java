package mainclient.fieldNowStatic;

import main.fieldNowStatic.FieldNowStatic;
import main.fieldNowStatic.FieldNowStaticSub;

public class FieldNowStaticFA {

	public String fieldNowStaticClientSimpleAccess() {
		FieldNowStatic f = new FieldNowStatic();
		return f.MODIFIED_FIELD;
	}
	
	public String fieldNowStaticClientSimpleAccessSub() {
		FieldNowStaticSub f = new FieldNowStaticSub();
		return f.MODIFIED_FIELD;
	}
}
