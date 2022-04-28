package genericsIfazeMethodTypeDelete;

import testing_lib.genericsIfazeMethodTypeDelete.GenericsIfazeMethodTypeDelete;

public class Main implements GenericsIfazeMethodTypeDelete {

	public static void main(String[] args) {
		Main constr = new Main();
		constr.<Integer>method1();
	}

	@Override
	public <T> void method1() {
		
	}
		
}
