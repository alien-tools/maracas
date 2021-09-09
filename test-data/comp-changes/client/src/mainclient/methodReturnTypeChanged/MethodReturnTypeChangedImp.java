package mainclient.methodReturnTypeChanged;

import java.util.ArrayList;

import main.methodReturnTypeChanged.IMethodReturnTypeChanged;

public class MethodReturnTypeChangedImp implements IMethodReturnTypeChanged {

	@Override
	public ArrayList methodReturnTypeChangedList() {
		return new ArrayList();
	}

	@Override
	public long methodReturnTypeChangedNumeric() {
		return 1;
	}

}
