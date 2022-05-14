package genericsClazzMethodTypeBoundsAdd;

import testing_lib.genericsClazzMethodTypeBoundsAdd.GenericsClazzMethodTypeBoundsAdd;

public class Main {


	public static void main(String[] args) {
		
		GenericsClazzMethodTypeBoundsAdd constr = new GenericsClazzMethodTypeBoundsAdd();
		constr.<Object>method1();
		
	}
	
}
