package mainclient.methodNowThrowsCheckedException;

import main.methodNowThrowsCheckedException.IMethodNowThrowsCheckedException;
import main.methodNowThrowsCheckedException.MethodNowThrowsCheckedException;
import main.methodNowThrowsCheckedException.MethodNowThrowsCheckedExceptionSub;

public class MethodNowThrowsCheckedExceptionMI {

	public void callSuperMethod() {
		MethodNowThrowsCheckedException m = new MethodNowThrowsCheckedExceptionExt();
		m.nowThrowsExcep();
	}
	
	public int callInterMethod() {
		IMethodNowThrowsCheckedException m = new MethodNowThrowsCheckedExceptionImp();
		return m.nowThrowsExcep();
	}
	
	public void callSubtypeMethod() {
		MethodNowThrowsCheckedExceptionSub m = new MethodNowThrowsCheckedExceptionSub();
		m.nowThrowsExcep();
	}
	
	public void callClientSubtypeMethod() {
		MethodNowThrowsCheckedExceptionExt m = new MethodNowThrowsCheckedExceptionExt();
		m.nowThrowsExcep();
	}
	
	public int callImpMethod() {
		MethodNowThrowsCheckedExceptionImp m = new MethodNowThrowsCheckedExceptionImp();
		return m.nowThrowsExcep();
	}
	
}
