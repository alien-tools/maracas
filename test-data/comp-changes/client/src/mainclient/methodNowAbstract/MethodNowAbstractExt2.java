package mainclient.methodNowAbstract;

import main.methodNowAbstract.MethodNowAbstractSub;

public class MethodNowAbstractExt2 extends MethodNowAbstractSub {

	@Override
	public int methodStayAbstract() {
		return 0;
	}

	public int methodNowAbstractClientSuperKey() {
		return super.methodNowAbstract();
	}
	
	public int methodNowAbstractClientNoSuperKey() {
		return methodNowAbstract();
	}
}
