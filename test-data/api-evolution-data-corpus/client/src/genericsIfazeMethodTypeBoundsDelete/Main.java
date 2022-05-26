package genericsIfazeMethodTypeBoundsDelete;

import testing_lib.genericsIfazeMethodTypeBoundsDelete.GenericsIfazeMethodTypeBoundsDelete;

public class Main implements GenericsIfazeMethodTypeBoundsDelete {

	public static void main(String[] args) {
		Main constr = new Main();
		constr.<Integer>method1();
	}

	@Override
	public <T extends Number> void method1() {
		
	}
	
}
