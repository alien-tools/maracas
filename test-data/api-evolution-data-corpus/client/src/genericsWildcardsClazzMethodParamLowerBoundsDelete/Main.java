package genericsWildcardsClazzMethodParamLowerBoundsDelete;

import java.util.ArrayList;

import testing_lib.genericsWildcardsClazzMethodParamLowerBoundsDelete.GenericsWildcardsClazzMethodParamLowerBoundsDelete;

public class Main {

	public static void main(String[] args) {
		GenericsWildcardsClazzMethodParamLowerBoundsDelete constr = new GenericsWildcardsClazzMethodParamLowerBoundsDelete();
		ArrayList<Number> param1 = new ArrayList<Number>();
		constr.method1(param1);
	}
	
}
