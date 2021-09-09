package mainclient.methodRemovedInSuperclass;

import main.methodRemovedInSuperclass.MethodRemovedInSuperclass;

public class MethodRemovedInSuperclassExt extends MethodRemovedInSuperclass {

	@Override
	public int methodRemovedSAbs() {
		return 0;
	}

	@Override
	public int methodRemovedSSAbs() {
		return 0;
	}

	public int callSuperSMethod() {
		return methodRemovedS();
	}
	
	public int callSuperSSMethod() {
		return methodRemovedSS();
	}
}
