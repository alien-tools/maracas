package mainclient.methodReturnTypeChanged;

import java.util.ArrayList;

import main.methodReturnTypeChanged.MethodReturnTypeChanged;

public class MethodReturnTypeChangedExt extends MethodReturnTypeChanged {

	public long numericClientSuperKey() {
		return super.methodReturnTypeChangedNumeric();
	}
	
	public ArrayList listClientSuperKey() {
		return super.methodReturnTypeChangedList();
	}
	
	public long numericClientNoSuperKey() {
		return methodReturnTypeChangedNumeric();
	}
	
	public ArrayList listClientNoSuperKey() {
		return methodReturnTypeChangedList();
	}
}
