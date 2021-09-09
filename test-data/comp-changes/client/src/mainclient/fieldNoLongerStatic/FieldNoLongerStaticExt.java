package mainclient.fieldNoLongerStatic;

import main.fieldNoLongerStatic.FieldNoLongerStatic;
import main.fieldNoLongerStatic.FieldNoLongerStaticSuper;

public class FieldNoLongerStaticExt extends FieldNoLongerStatic {

	public int fieldNoLongerStaticClientNoSuperKey() {
		return fieldStatic;
	}
	
	public int fieldNoLongerStaticClientSuperKey() {
		return super.fieldStatic;
	}

	public int fieldNoLongerStaticClientStatic() {
		return FieldNoLongerStatic.fieldStatic;
	}

	public int fieldNoLongerStaticClientSuperNoSuperKey() {
		return superFieldStatic;
	}
	
	public int fieldNoLongerStaticClientSuperSuperKey() {
		return super.superFieldStatic;
	}
	
	public int fieldNoLongerStaticClientSuperStatic() {
		return FieldNoLongerStaticSuper.superFieldStatic;
	}
}
