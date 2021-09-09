package mainclient.methodNowAbstract;

import main.methodNowAbstract.IMethodNowAbstract;
import main.methodNowAbstract.IMethodNowAbstractSub;
import main.methodNowAbstract.MethodNowAbstract;
import main.methodNowAbstract.MethodNowAbstractSub;

public class MethodNowAbstractMI {
	public void instantiateNowAbstractAnonymous() {
		MethodNowAbstract c = new MethodNowAbstract() {
			@Override
			public int methodStayAbstract() {
				// TODO Auto-generated method stub
				return 0;
			}};
	}

	public void instantiateNowAbstractAnonymousAlreadyImplemented() {
		MethodNowAbstract c = new MethodNowAbstract() {
			@Override
			public int methodStayAbstract() {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public int methodNowAbstract() {
				// TODO Auto-generated method stub
				return 0;
			}};
	}
	
	public void instantiateNowAbstractSubAnonymous() {
		MethodNowAbstract c = new MethodNowAbstractSub() {
			@Override
			public int methodStayAbstract() {
				// TODO Auto-generated method stub
				return 0;
			}};
	}

	public void instantiateNowAbstractSubAnonymousAlreadyImplemented() {
		MethodNowAbstract c = new MethodNowAbstractSub() {
			@Override
			public int methodStayAbstract() {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public int methodNowAbstract() {
				// TODO Auto-generated method stub
				return 0;
			}};
	}
	
	public void instantiateNowAbstractIntfAnonymous() {
		IMethodNowAbstract c = new IMethodNowAbstract() {
			@Override
			public int methodStayAbstract() {
				// TODO Auto-generated method stub
				return 0;
			}};
	}

	public void instantiateNowAbstractINtfAnonymousAlreadyImplemented() {
		IMethodNowAbstract c = new IMethodNowAbstract() {
			@Override
			public int methodStayAbstract() {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public int methodNowAbstract() {
				// TODO Auto-generated method stub
				return 0;
			}};
	}
	
	public void instantiateNowAbstractIntfSubAnonymous() {
		IMethodNowAbstractSub c = new IMethodNowAbstractSub() {
			@Override
			public int methodStayAbstract() {
				// TODO Auto-generated method stub
				return 0;
			}};
	}

	public void instantiateNowAbstractIntfSubAnonymousAlreadyImplemented() {
		IMethodNowAbstractSub c = new IMethodNowAbstractSub() {
			@Override
			public int methodStayAbstract() {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public int methodNowAbstract() {
				// TODO Auto-generated method stub
				return 0;
			}};
	}
}
