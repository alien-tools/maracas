package mainclient.methodAbstractNowDefault;

import main.methodAbstractNowDefault.IMethodAbstractNowDefault;
import main.methodAbstractNowDefault.IMethodAbstractNowDefaultOther;

public class MethodAbstractNowDefaultMultiInt implements IMethodAbstractNowDefault, IMethodAbstractNowDefaultOther {

	@Override
	public int methodAbstractNowDef() {
		return 0;
	}
	
	public int callMethod() {
		return methodAbstractNowDef();
	}

}
