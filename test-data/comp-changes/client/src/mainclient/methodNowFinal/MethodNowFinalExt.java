package mainclient.methodNowFinal;

import main.methodNowFinal.MethodNowFinal;

public class MethodNowFinalExt extends MethodNowFinal {

	@Override
	public int methodNowFinal() {
		return 1;
	}
	
	@Override
	public int sMethodNowFinal() {
		return 10;
	}
	
	public int methodNowFinalClient() {
		return super.methodNowFinal();
	}
	
	public int sMethodNowFinalNoOverride() {
		return 10;
	}
}
