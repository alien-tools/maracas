package mainclient.methodNewDefault;

import main.methodNewDefault.IMethodNewDefaultOther;
import main.methodNewDefault.IMethodNewDefaultSub;

public class MethodNewDefaultMultiIntSub implements IMethodNewDefaultSub, IMethodNewDefaultOther {

	public int callDefaultMethodOther() {
		return defaultMethod();
	}
	
	public int callDefaultMethodOtherSuper() {
		return IMethodNewDefaultOther.super.defaultMethod();
	}
}
