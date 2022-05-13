package genericsIfazeMethodTypeBoundsGeneralization;

import testing_lib.genericsIfazeMethodTypeBoundsGeneralization.GenericsIfazeMethodTypeBoundsGeneralization;

public class Main implements GenericsIfazeMethodTypeBoundsGeneralization {

	public static void main(String[] args) {
		Main constr = new Main();
		constr.<Integer>method1();
	}

	@Override
	public <T extends Integer> void method1() {

	}
	
}
