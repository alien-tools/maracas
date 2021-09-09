package mainclient.methodRemoved;

import main.methodRemoved.MethodRemoved;

public class MethodRemovedExt extends MethodRemoved {

	public int methodRemovedClientExt() {
		return methodRemoved();
	}

	public int methodRemovedClientSuper() {
		return super.methodRemoved();
	}

	@Override
	public int methodRemoved() {
		return super.methodRemoved();
	}

	@Override
	public int methodStay() {
		return super.methodStay();
	}
}
