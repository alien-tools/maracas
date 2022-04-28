package genericsIfazeMethodTypeBoundsSpecialization;

import testing_lib.genericsIfazeMethodTypeBoundsSpecialization.GenericsIfazeMethodTypeBoundsSpecialization;

public class Main implements GenericsIfazeMethodTypeBoundsSpecialization {

	public static void main(String[] args) {
		Main constr = new Main();
		constr.<Number>method1();
	}

	@Override
	public <T extends Number> void method1() {
		
	}
	
}
