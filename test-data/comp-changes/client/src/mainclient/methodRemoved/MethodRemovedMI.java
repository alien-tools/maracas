package mainclient.methodRemoved;

import main.methodRemoved.MethodRemoved;

public class MethodRemovedMI {

	public int methodRemovedClient() {
		MethodRemoved m = new MethodRemoved();
		return m.methodRemoved();
	}

	public int methodRemovedStatic() {
		return MethodRemoved.methodRemovedStatic();
	}

}
