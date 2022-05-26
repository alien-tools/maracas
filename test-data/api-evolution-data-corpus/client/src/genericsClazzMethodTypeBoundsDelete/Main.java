package genericsClazzMethodTypeBoundsDelete;

import testing_lib.genericsClazzMethodTypeBoundsDelete.GenericsClazzMethodTypeBoundsDelete;

public class Main {
	
	public static void main(String[] args) {
		GenericsClazzMethodTypeBoundsDelete constr = new GenericsClazzMethodTypeBoundsDelete();
		constr.<Integer>method1();
	}
}
