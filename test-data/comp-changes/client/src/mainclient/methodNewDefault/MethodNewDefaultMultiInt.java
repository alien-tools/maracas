package mainclient.methodNewDefault;

import main.methodNewDefault.IMethodNewDefault;
import main.methodNewDefault.IMethodNewDefaultOther;

public class MethodNewDefaultMultiInt implements IMethodNewDefault, IMethodNewDefaultOther {

	public int callDefaultMethodOther() {
		return defaultMethod();
	}
	
	public int callDefaultMethodOtherSuper() {
		return IMethodNewDefaultOther.super.defaultMethod();
	}
}
