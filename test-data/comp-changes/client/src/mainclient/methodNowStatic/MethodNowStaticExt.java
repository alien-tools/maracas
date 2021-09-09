package mainclient.methodNowStatic;

import main.methodNowStatic.MethodNowStatic;

public class MethodNowStaticExt extends MethodNowStatic {

	public int methodNowStaticClientSuperKeyAccess() {
		return super.methodNowStatic();
	}
	
	public int methodNowStaticClientNoSuperKeyAccess() {
		return methodNowStatic();
	}
}
