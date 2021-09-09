package mainclient.classNowCheckedException;

import main.classNowCheckedException.ClassNowCheckedException;
import main.classNowCheckedException.ClassNowCheckedExceptionSub;

public class ClassNowCheckedExceptionThrows {

	public int throwsExcep(boolean b) {
		if (b) {
			return 0;
		}
		else {
			throw new ClassNowCheckedException();
		}
	}
	
	public int throwsSubExcep(boolean b) {
		if (b) {
			return 0;
		}
		else {
			throw new ClassNowCheckedExceptionSub();
		}
	}
	
	public int throwsClientExcep(boolean b) {
		if (b) {
			return 0;
		}
		else {
			throw new ClassNowCheckedExceptionClient();
		}
	}
	
	public int throwsClientSubExcep(boolean b) {
		if (b) {
			return 0;
		}
		else {
			throw new ClassNowCheckedExceptionClientSub();
		}
	}
	
	public int throwsVariableException(boolean b) {
		ClassNowCheckedException e = new ClassNowCheckedException();
		if (b) {
			return 0;
		}
		else {
			throw e;
		}
	}
	
	public int throwsExcepChecked(boolean b) throws ClassNowCheckedException {
		if (b) {
			return 0;
		}
		else {
			throw new ClassNowCheckedException();
		}
	}
	
	public int throwsSubExcepChecked(boolean b) throws ClassNowCheckedExceptionSub {
		if (b) {
			return 0;
		}
		else {
			throw new ClassNowCheckedExceptionSub();
		}
	}
	
	public int throwsClientExcepChecked(boolean b) throws ClassNowCheckedExceptionClient {
		if (b) {
			return 0;
		}
		else {
			throw new ClassNowCheckedExceptionClient();
		}
	}
	
	public int throwsClientSubExcepChecked(boolean b) throws ClassNowCheckedExceptionClientSub {
		if (b) {
			return 0;
		}
		else {
			throw new ClassNowCheckedExceptionClientSub();
		}
	}
	
	public int throwsVariableExceptionChecked(boolean b) throws ClassNowCheckedException {
		ClassNowCheckedException e = new ClassNowCheckedException();
		if (b) {
			return 0;
		}
		else {
			throw e;
		}
	}
	
	public int throwsExcepCaughtSameType(boolean b) {
		if (b) {
			return 0;
		}
		else {
			try {
				throw new ClassNowCheckedException();
			} catch (ClassNowCheckedException e) {
				return 1;
			}
		}
	}
	
	public int throwExcepCaughtExceptionType(boolean b) {
		if (b) {
			return 0;
		}
		else {
			try {
				throw new ClassNowCheckedException();
			} catch (Exception e) {
				return 1;
			}
		}
	}
	
	public int throwsExcepCheckedType(boolean b) throws Exception {
		if (b) {
			return 0;
		}
		else {
			throw new ClassNowCheckedException();
		}
	}
}
