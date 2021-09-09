package mainclient.fieldNoLongerStatic;

import main.fieldNoLongerStatic.FieldNoLongerStatic;
import main.fieldNoLongerStatic.FieldNoLongerStaticSuper;

public class FieldNoLongerStaticFA {

	public int fieldNoLongerStaticClient() {
		return FieldNoLongerStatic.fieldStatic;
	}
	
	public int fieldNoLongerStaticSuperClient1() {
		return FieldNoLongerStatic.superFieldStatic;
	}

	public int fieldNoLongerStaticSuperClient2() {
		return FieldNoLongerStaticSuper.superFieldStatic;
	}
	
	public int fieldNoLongerStaticInstanceAccess() {
		FieldNoLongerStatic f = new FieldNoLongerStatic();
		return f.fieldStatic;
	}
	
	public int fieldNoLongerStaticInstanceAccessSuper1() {
		FieldNoLongerStatic f = new FieldNoLongerStatic();
		return f.superFieldStatic;
	}
	
	public int fieldNoLongerStaticInstanceAccessSuper2() {
		FieldNoLongerStaticSuper f = new FieldNoLongerStaticSuper();
		return f.superFieldStatic;
	}
}
