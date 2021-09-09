package mainclient.methodNowAbstract;

import main.methodNowAbstract.MethodNowAbstractSub;

public class MethodNowAbstractExtWithImpl2 extends MethodNowAbstractSub {

	@Override
	public int methodStayAbstract() {
		return 0;
	}
	
	@Override
	public int methodNowAbstract() {
		return 1;
	}
}
