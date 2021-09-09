package mainclient.fieldStaticAndOverridesStatic;

import main.fieldStaticAndOverridesStatic.FieldStaticAndOverridesStaticMulti;

public class FieldStaticAndOverridesStaticMultiExt extends FieldStaticAndOverridesStaticMulti {

	public int accessFieldFromSubtypeSuper() {
		return super.fieldStatic;
	}
	
	public int accessFieldFromSubtypeNoSuper() {
		return fieldStatic;
	}
	
}
