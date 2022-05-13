package inheritanceIfazeMethodMovedFromSuperInterface;

import testing_lib.inheritanceIfazeMethodMovedFromSuperInterface.InheritanceIfazeMethodMovedFromSuperInterface;

public class Main implements InheritanceIfazeMethodMovedFromSuperInterface{

	@Override
	public void method1() {
		
	}

	public static void main(String[] args) {
		InheritanceIfazeMethodMovedFromSuperInterface ifaze = new Main();
		ifaze.method1();
	}
	
}
