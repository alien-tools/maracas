package mainclient.superclassRemoved;

import java.util.List;

import main.interfaceRemoved.IInterfaceRemoved;
import main.superclassRemoved.SuperSuperclassRemoved;
import main.superclassRemoved.SuperclassRemoved;

public class SuperclassRemovedExt extends SuperclassRemoved {

	public void cast() {
		SuperclassRemoved a = new SuperclassRemoved();
		SuperSuperclassRemoved i = (SuperSuperclassRemoved) a;
	}
	
	public void intCons() {
		int ia = SuperclassRemoved.CTE;
	}
	
	public void intConsSuper() {
		int ii = SuperSuperclassRemoved.CTE;
	}
	
	public void intConsDirect() {
		int in = CTE;
	}
	
	public void listCons() {
		List<String> la = SuperclassRemoved.LIST;
	}
	
	public void listConsSuper() {
		List<String> li = SuperSuperclassRemoved.LIST;
	}
	
	public void listConsDirect() {
		List<String> ln = LIST;
	}
	
	public void staticM() {
		int ia = SuperclassRemoved.staticMeth();
	}
	
	public void staticMSuper() {
		int ii = SuperSuperclassRemoved.staticMeth();
	}
	
	public void staticMDirect() {
		int in = staticMeth();
	}
}
