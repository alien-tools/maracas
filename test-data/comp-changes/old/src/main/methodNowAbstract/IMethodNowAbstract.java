package main.methodNowAbstract;

public interface IMethodNowAbstract {

	public int methodStayAbstract();
	
	public default int methodNowAbstract() {
		return 0;
	}
}
