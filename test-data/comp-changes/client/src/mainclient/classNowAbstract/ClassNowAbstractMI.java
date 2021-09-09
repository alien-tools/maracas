package mainclient.classNowAbstract;

import main.classNowAbstract.ClassNowAbstract;

public class ClassNowAbstractMI {

	public void createObject() {
		ClassNowAbstract c = new ClassNowAbstract();
	}
	
	public void createObjectParams() {
		ClassNowAbstract c = new ClassNowAbstract(3);
	}

	public void createSubObject() {
		ClassNowAbstractExt c = new ClassNowAbstractExt(3);
	}
	
	public void accessConstant() {
		int i = ClassNowAbstract.CTE;
	}

	public void createObjectAnonymous() {
		ClassNowAbstract c = new ClassNowAbstract() {};
	}

	public void createObjectAnonymousParams() {
		ClassNowAbstract c = new ClassNowAbstract(3) {};
	}
}
