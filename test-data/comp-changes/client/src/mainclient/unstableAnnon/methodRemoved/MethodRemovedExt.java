package mainclient.unstableAnnon.methodRemoved;

import main.unstableAnnon.methodRemoved.MethodRemoved;

public class MethodRemovedExt extends MethodRemoved {

	public int methodRemovedClientExt() {
		return methodRemoved();
	}
	
	public int methodRemovedClientSuper() {
		return super.methodRemoved();
	}
}
