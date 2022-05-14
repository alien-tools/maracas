package modifierFieldNonTransientToTransient;

import testing_lib.modifierFieldNonTransientToTransient.ModifierFieldNonTransientToTransient;

public class Main {

	public static void main(String[] args) {
		ModifierFieldNonTransientToTransient constr = new ModifierFieldNonTransientToTransient();
		Integer test = constr.field1;
	}
	
}
