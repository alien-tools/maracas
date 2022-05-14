package genericsClazzMethodTypeBoundsAddSecond;

import testing_lib.genericsClazzMethodTypeBoundsAddSecond.GenericsClazzMethodTypeBoundsAddSecond;

public class Main {

	public static void main(String[] args) {
		GenericsClazzMethodTypeBoundsAddSecond constr = new GenericsClazzMethodTypeBoundsAddSecond();
		constr.<Number>method1();
	}
	
}
