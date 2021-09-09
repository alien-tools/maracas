package mainclient.superclassRemoved;

import java.util.List;

import main.interfaceRemoved.IInterfaceRemoved;
import main.interfaceRemoved.InterfaceRemoved;
import main.superclassRemoved.SuperSuperclassRemoved;
import main.superclassRemoved.SuperclassRemoved;

public class SuperclassRemovedTD {

	public void cast() {
		SuperclassRemoved a = new SuperclassRemoved();
		SuperSuperclassRemoved i = (SuperSuperclassRemoved) a;
	}
	
	public void intCons() {
		int ia = SuperclassRemoved.CTE;
	}
	
	public void intConsInter() {
		int ii = SuperSuperclassRemoved.CTE;
	}
	
	public void listCons() {
		List<String> la = SuperclassRemoved.LIST;
	}
	
	public void listConsInter() {
		List<String> li = SuperSuperclassRemoved.LIST;
	}
	
	public void staticM() {
		int ia = SuperclassRemoved.staticMeth();
	}
	
	public void staticMSuper() {
		int ii = SuperSuperclassRemoved.staticMeth();
	}

}
