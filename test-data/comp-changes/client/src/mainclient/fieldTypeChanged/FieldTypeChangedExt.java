package mainclient.fieldTypeChanged;

import main.fieldTypeChanged.A;
import main.fieldTypeChanged.FieldTypeChanged;

public class FieldTypeChangedExt extends FieldTypeChanged {

	public void accessAndAssign() {
		int a1 = unchangedPrimitive;
		int a2 = this.unchangedPrimitive;
		int a3 = super.unchangedPrimitive;
		A b1 = unchangedReference;
		A b2 = this.unchangedReference;
		A b3 = super.unchangedReference;

		int c1 = changedPrimitive;
		int c2 = this.changedPrimitive;
		int c3 = super.changedPrimitive;
		A d1 = changedReference;
		A d2 = this.changedReference;
		A d3 = super.changedReference;
	}
}
