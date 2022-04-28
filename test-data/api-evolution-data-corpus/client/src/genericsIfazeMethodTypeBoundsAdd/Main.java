package genericsIfazeMethodTypeBoundsAdd;

import testing_lib.genericsIfazeMethodTypeBoundsAdd.GenericsIfazeMethodTypeBoundsAdd;

public class Main implements GenericsIfazeMethodTypeBoundsAdd{

	public static void main(String[] args) {
		Main constr = new Main();
		constr.<Integer>method1();
	}

	@Override
	public <T> void method1() {
		
	}

}
