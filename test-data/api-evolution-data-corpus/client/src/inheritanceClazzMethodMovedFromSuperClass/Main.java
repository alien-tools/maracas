package inheritanceClazzMethodMovedFromSuperClass;

import testing_lib.inheritanceClazzMethodMovedFromSuperClass.Clazz1;
import testing_lib.inheritanceClazzMethodMovedFromSuperClass.InheritanceClazzMethodMovedFromSuperClass;

public class Main {

	public static void main(String[] args) {
		
		Clazz1 constr = new InheritanceClazzMethodMovedFromSuperClass();
		constr.method1();
		
		// This calling is OK
//		InheritanceClazzMethodMovedFromSuperClass constr = new InheritanceClazzMethodMovedFromSuperClass();
//		constr.method1();
	}
	
}
