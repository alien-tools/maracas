package genericsIfazeMethodTypeSwap;

import testing_lib.genericsIfazeMethodTypeSwap.GenericsIfazeMethodTypeSwap;

public class Main implements GenericsIfazeMethodTypeSwap {

	public static void main(String[] args) {
		Main constr = new Main();
		constr.<Integer, String>method1();
	}

	@Override
	public <T, K> void method1() {
		// TODO Auto-generated method stub
		
	}
	
}
