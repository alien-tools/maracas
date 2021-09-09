package mainclient.classTypeChanged;

import main.classTypeChanged.ClassTypeChangedC2A;
import main.classTypeChanged.ClassTypeChangedC2E;
import main.classTypeChanged.ClassTypeChangedC2I;

public class ClassTypeChangedC2AllTD {

	ClassTypeChangedC2A c2a;
	ClassTypeChangedC2I c2i;
	ClassTypeChangedC2E c2e;
	
	public void e2aDep() {
		ClassTypeChangedC2A c2a;
	}
	
	public void e2iDep() {
		ClassTypeChangedC2I c2i;
	}
	
	public void e2cDep() {
		ClassTypeChangedC2E c2e;
	}
}
