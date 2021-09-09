package mainclient.methodNewDefault;

import main.methodNewDefault.IMethodNewDefault;
import main.methodNewDefault.IMethodNewDefaultOther;

public class MethodNewDefaultMultiIntOwnDef implements IMethodNewDefault, IMethodNewDefaultOther {

	public int defaultMethod() {
		return 11;
	}
	
	public int callOwnDefaultMethod() {
		return defaultMethod();
	}
	
}
