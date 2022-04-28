package genericsClazzMethodTypeBoundsSpecialization;

import testing_lib.genericsClazzMethodTypeBoundsSpecialization.GenericsClazzMethodTypeBoundsSpecialization;

public class Main {

	public static void main(String[] args) {
		GenericsClazzMethodTypeBoundsSpecialization constr = new GenericsClazzMethodTypeBoundsSpecialization();
		constr.<Number>method1();
	}
	
}
