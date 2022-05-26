package genericsIfazeMethodTypeAddSecond;

import testing_lib.genericsIfazeMethodTypeAddSecond.GenericsIfazeMethodTypeAddSecond;

public class Main implements GenericsIfazeMethodTypeAddSecond {

	public static void main(String[] args) {
		Main constr = new Main();
		constr.<Integer>method1();
	}

	@Override
	public <T> void method1() {
		
	}

}
