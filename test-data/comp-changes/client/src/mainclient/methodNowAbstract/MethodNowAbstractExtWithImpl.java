package mainclient.methodNowAbstract;

import main.methodNowAbstract.MethodNowAbstract;

public class MethodNowAbstractExtWithImpl extends MethodNowAbstract {

	@Override
	public int methodStayAbstract() {
		return 0;
	}
	
	@Override
	public int methodNowAbstract() {
		return 1;
	}
}
