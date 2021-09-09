package mainclient.methodNoLongerStatic;

import main.methodNoLongerStatic.IMethodNoLongerStatic;

public class MethodNoLongerStaticImp implements IMethodNoLongerStatic {
	
	public int methodNoLongerStaticClient() {
		return IMethodNoLongerStatic.methodNoLongerStatic();
	}
}
