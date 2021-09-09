package mainclient.methodReturnTypeChanged;

import java.util.ArrayList;

import main.methodReturnTypeChanged.MethodReturnTypeChanged;

public class MethodReturnTypeChangedMI {

	public long numericClient() {
		MethodReturnTypeChanged m = new MethodReturnTypeChanged();
		return m.methodReturnTypeChangedNumeric();
	}
	
	public ArrayList listClient() {
		MethodReturnTypeChanged m = new MethodReturnTypeChanged();
		return m.methodReturnTypeChangedList();
	}
	
}
