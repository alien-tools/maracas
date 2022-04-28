package genericsIfazeMethodTypeBoundsMutation;

import testing_lib.genericsIfazeMethodTypeBoundsMutation.GenericsIfazeMethodTypeBoundsMutation;

public class Main implements GenericsIfazeMethodTypeBoundsMutation {

	public static void main(String[] args) {
		Main constr = new Main();
		constr.<Integer>method1();
	}

	@Override
	public <T extends Integer> void method1() {
		
	}
	
}
