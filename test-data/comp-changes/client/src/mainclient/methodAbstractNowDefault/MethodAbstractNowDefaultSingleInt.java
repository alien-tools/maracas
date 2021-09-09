package mainclient.methodAbstractNowDefault;

import main.methodAbstractNowDefault.IMethodAbstractNowDefault;

public class MethodAbstractNowDefaultSingleInt implements IMethodAbstractNowDefault {

	@Override
	public int methodAbstractNowDef() {
		return 0;
	}

	public int callMethod() {
		return methodAbstractNowDef();
	}
}
