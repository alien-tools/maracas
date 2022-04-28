package genericsClazzMethodTypeBoundsGeneralization;

import testing_lib.genericsClazzMethodTypeBoundsGeneralization.GenericsClazzMethodTypeBoundsGeneralization;

public class Main {

	public static void main(String[] args) {
		GenericsClazzMethodTypeBoundsGeneralization constr = new GenericsClazzMethodTypeBoundsGeneralization();
		constr.<Integer>method1();
	}
	
}
