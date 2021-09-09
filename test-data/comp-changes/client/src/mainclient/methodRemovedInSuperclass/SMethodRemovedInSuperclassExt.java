package mainclient.methodRemovedInSuperclass;

import main.methodRemovedInSuperclass.SMethodRemovedInSuperclass;

public class SMethodRemovedInSuperclassExt extends SMethodRemovedInSuperclass {

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
