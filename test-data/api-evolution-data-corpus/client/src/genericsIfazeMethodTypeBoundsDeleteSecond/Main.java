package genericsIfazeMethodTypeBoundsDeleteSecond;

import testing_lib.genericsIfazeMethodTypeBoundsDeleteSecond.GenericsIfazeMethodTypeBoundsDeleteSecond;

public class Main implements GenericsIfazeMethodTypeBoundsDeleteSecond{

	public static void main(String[] args) {
		Main constr = new Main();
		constr.<Integer>method1();
	}

	@Override
	public <T extends Number & Comparable<T>> void method1() {
		
	}
	
}
