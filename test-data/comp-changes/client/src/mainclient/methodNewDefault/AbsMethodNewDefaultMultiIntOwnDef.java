package mainclient.methodNewDefault;

import main.methodNewDefault.IMethodNewDefault;
import main.methodNewDefault.IMethodNewDefaultOther;

public abstract class AbsMethodNewDefaultMultiIntOwnDef implements IMethodNewDefault, IMethodNewDefaultOther {

	public int defaultMethod() {
		return 11;
	}
	
}
