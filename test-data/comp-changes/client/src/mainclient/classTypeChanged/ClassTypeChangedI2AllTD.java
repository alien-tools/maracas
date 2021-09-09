package mainclient.classTypeChanged;

import main.classTypeChanged.ClassTypeChangedI2A;
import main.classTypeChanged.ClassTypeChangedI2C;
import main.classTypeChanged.ClassTypeChangedI2E;

public class ClassTypeChangedI2AllTD {

	ClassTypeChangedI2A i2a;
	ClassTypeChangedI2C i2c;
	ClassTypeChangedI2E i2e;
	
	public void e2aDep() {
		ClassTypeChangedI2A i2a;
	}
	
	public void e2iDep() {
		ClassTypeChangedI2C i2c;
	}
	
	public void e2cDep() {
		ClassTypeChangedI2E i2e;
	}
}
