package mainclient.methodNewDefault;

import main.methodNewDefault.IMethodNewDefault;

public class MethodNewDefaultOwnDef implements IMethodNewDefault {

	public int defaultMethod() {
		return 11;
	}
	
	public int callOwnDefaultMethod() {
		return defaultMethod();
	}
	
}
