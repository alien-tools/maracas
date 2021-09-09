package mainclient.fieldStaticAndOverridesStatic;

import main.fieldStaticAndOverridesStatic.FieldStaticAndOverridesStatic;

public class FieldStaticAndOverridesStaticExt extends FieldStaticAndOverridesStatic {
	
	public int accessFieldFromSubtypeSuper() {
		return super.fieldStatic;
	}
	
	public int accessFieldFromSubtypeNoSuper() {
		return fieldStatic;
	}
}
