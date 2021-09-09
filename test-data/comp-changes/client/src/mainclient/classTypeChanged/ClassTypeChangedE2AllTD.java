package mainclient.classTypeChanged;

import main.classTypeChanged.ClassTypeChangedE2A;
import main.classTypeChanged.ClassTypeChangedE2C;
import main.classTypeChanged.ClassTypeChangedE2I;

public class ClassTypeChangedE2AllTD {

	ClassTypeChangedE2A e2a;
	ClassTypeChangedE2I e2i;
	ClassTypeChangedE2C e2c;
	
	public void e2aDep() {
		ClassTypeChangedE2A e2a;
	}
	
	public void e2iDep() {
		ClassTypeChangedE2I e2i;
	}
	
	public void e2cDep() {
		ClassTypeChangedE2C e2c;
	}
}
