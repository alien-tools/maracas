package mainclient.interfaceRemoved;

import java.util.List;

import main.interfaceRemoved.IInterfaceRemoved;
import main.interfaceRemoved.InterfaceRemoved;

public class InterfaceRemovedExt extends InterfaceRemoved {

	public void cast() {
		InterfaceRemoved a = new InterfaceRemoved();
		IInterfaceRemoved i = (IInterfaceRemoved) a;
	}
	
	public void intCons() {
		int ia = InterfaceRemoved.CTE;
	}
	
	public void intConsInter() {
		int ii = IInterfaceRemoved.CTE;
	}
	
	public void intConsDirect() {
		int in = CTE;
	}
	
	public void listCons() {
		List<String> la = InterfaceRemoved.LIST;
	}
	
	public void listConsInter() {
		List<String> li = IInterfaceRemoved.LIST;
	}
	
	public void listConsDirect() {
		List<String> ln = LIST;
	}
	
	public void staticM() {
		//int ia = InterfaceRemoved.staticMeth(); Cannot happen
		int ii = IInterfaceRemoved.staticMeth();
		//int in = staticMethos(); Cannot happen
	}
	
	public void defaultM() {
		//int ia = InterfaceRemoved.defaultMeth(); Cannot happen
		//int ii = IInterfaceRemoved.super.defaultMeth(); Cannot happen
		int in = defaultMeth();
	}
}
