package genericsClazzMethodTypeBoundsMutation;

import testing_lib.genericsClazzMethodTypeBoundsMutation.GenericsClazzMethodTypeBoundsMutation;

public class Main {

	public static void main(String[] args) {
		GenericsClazzMethodTypeBoundsMutation constr = new GenericsClazzMethodTypeBoundsMutation();
		constr.<Integer>method1();
	}
	
}
