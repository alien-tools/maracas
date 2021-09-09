package mainclient.methodNoLongerThrowsCheckedException;

import java.io.IOException;

import main.methodNoLongerThrowsCheckedException.IMethodNoLongerThrowsCheckedException;
import main.methodNoLongerThrowsCheckedException.MethodNoLongerThrowsCheckedException;
import main.methodNoLongerThrowsCheckedException.MethodNoLongerThrowsCheckedExceptionSub;

public class MethodNoLongerThrowsCheckedExceptionMI {

	public void callSuperMethod() {
		MethodNoLongerThrowsCheckedException m = new MethodNoLongerThrowsCheckedExceptionExt();
		try {
			m.noLongerThrowsExcep();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public int callInterMethod() {
		IMethodNoLongerThrowsCheckedException m = new MethodNoLongerThrowsCheckedExceptionImp();
		try {
			return m.noLongerThrowsExcep();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return 0;
	}
	
	public void callSubtypeMethod() {
		MethodNoLongerThrowsCheckedExceptionSub m = new MethodNoLongerThrowsCheckedExceptionSub();
		try {
			m.noLongerThrowsExcep();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void callClientSubtypeMethod() {
		MethodNoLongerThrowsCheckedExceptionExt m = new MethodNoLongerThrowsCheckedExceptionExt();
		try {
			m.noLongerThrowsExcep();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public int callImpMethod() {
		MethodNoLongerThrowsCheckedExceptionImp m = new MethodNoLongerThrowsCheckedExceptionImp();
		try {
			return m.noLongerThrowsExcep();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
}
