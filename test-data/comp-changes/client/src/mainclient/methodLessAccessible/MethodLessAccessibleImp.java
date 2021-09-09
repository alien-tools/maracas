package mainclient.methodLessAccessible;

import main.methodLessAccessible.IMethodLessAccessible;

public class MethodLessAccessibleImp implements IMethodLessAccessible {

	@Override
	public int methodLessAccessiblePackPriv2Public() {
		return 0;
	}

	@Override
	public int methodLessAccessiblePublic2PackPriv() {
		return 0;
	}

}
