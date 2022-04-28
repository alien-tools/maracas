package genericsIfazeMethodTypeDeleteSecond;

import testing_lib.genericsIfazeMethodTypeDeleteSecond.GenericsIfazeMethodTypeDeleteSecond;

public class Main implements GenericsIfazeMethodTypeDeleteSecond {

	public static void main(String[] args) {
		Main constr = new Main();
		constr.<Integer, String>method1();
	}

	@Override
	public <T, K> void method1() {

	}
	
}
