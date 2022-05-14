package inheritanceIfazeMethodMovedToSuperInterface;

import testing_lib.inheritanceIfazeMethodMovedToSuperInterface.InheritanceIfazeMethodMovedToSuperInterface;

public class Main implements InheritanceIfazeMethodMovedToSuperInterface {

	@Override
	public void method1() {
		
	}

	public static void main(String[] args) {
		InheritanceIfazeMethodMovedToSuperInterface ifaze = new Main();
		ifaze.method1();
	}
	
}
