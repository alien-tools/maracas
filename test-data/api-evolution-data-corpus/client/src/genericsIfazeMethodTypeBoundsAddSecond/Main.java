package genericsIfazeMethodTypeBoundsAddSecond;

import testing_lib.genericsIfazeMethodTypeBoundsAddSecond.GenericsIfazeMethodTypeBoundsAddSecond;

public class Main implements GenericsIfazeMethodTypeBoundsAddSecond {

	public static void main(String[] args) {
		Main constr = new Main();
		constr.<Number>method1();
	}

	@Override
	public <T extends Number> void method1() {
		
	}
	
}
