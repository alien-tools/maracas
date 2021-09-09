package mainclient.interfaceAdded;

import java.util.List;

import main.interfaceAdded.IInterfaceAdded;
import main.interfaceAdded.InterfaceAdded;

public class InterfaceAddedExt extends InterfaceAdded {
	
	public void intConsInter() {
		int ii = IInterfaceAdded.CTE;
	}

	public void listConsInter() {
		List<String> li = IInterfaceAdded.LIST;
	}

	public void staticM() {
		//int ia = InterfaceRemoved.staticMeth(); Cannot happen
		int ii = IInterfaceAdded.staticMeth();
		//int in = staticMethos(); Cannot happen
	}

}
