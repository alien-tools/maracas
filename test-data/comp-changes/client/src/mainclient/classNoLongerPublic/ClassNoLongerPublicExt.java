package mainclient.classNoLongerPublic;

import main.classNoLongerPublic.ClassNoLongerPublic;

public class ClassNoLongerPublicExt extends ClassNoLongerPublic {

	public void accessNoSuperField() {
		int i = field;
	}
	
	public void accessSuperField() {
		int i = super.field;
	}
	
	public void accessNoSuperMethod() {
		int i = method();
	}
	
	public void accessSuperMethod() {
		int i = super.method();
	}
}
