package mainclient.fieldNowFinal;

import main.fieldNowFinal.FieldNowFinal;
import main.fieldNowFinal.FieldNowFinalSub;

public class FieldNowFinalFA {

	public int fieldNowFinalAssignment() {
		FieldNowFinal f = new FieldNowFinal();
		f.fieldFinal = 3;
		return f.fieldFinal;
	}
	
	public int fieldNowFinalNoAssignment() {
		FieldNowFinal f = new FieldNowFinal();
		return f.fieldFinal;
	}
	
	public int fieldNowFinalAssignmentSub() {
		FieldNowFinalSub f = new FieldNowFinalSub();
		f.fieldFinal = 3;
		return f.fieldFinal;
	}
	
	public int fieldNowFinalNoAssignmentSub() {
		FieldNowFinalSub f = new FieldNowFinalSub();
		return f.fieldFinal;
	}
}
