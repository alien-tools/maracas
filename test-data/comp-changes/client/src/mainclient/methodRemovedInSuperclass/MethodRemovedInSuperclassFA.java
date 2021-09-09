package mainclient.methodRemovedInSuperclass;

import main.methodRemovedInSuperclass.MethodRemovedInSuperclass;
import main.methodRemovedInSuperclass.SMethodRemovedInSuperclass;

public class MethodRemovedInSuperclassFA {

	public int accessSuperS() {
		SMethodRemovedInSuperclass s = new MethodRemovedInSuperclassExt();
		return s.methodRemovedSS();
	}
	
	public int accessSuperSAbs() {
		SMethodRemovedInSuperclass s = new MethodRemovedInSuperclassExt();
		return s.methodRemovedSSAbs();
	}
	
	public int accessSuper() {
		MethodRemovedInSuperclass s = new MethodRemovedInSuperclassExt();
		return s.methodRemovedS();
	}
	
	public int accessSuperAbs() {
		MethodRemovedInSuperclass s = new MethodRemovedInSuperclassExt();
		return s.methodRemovedSAbs();
	}
}
