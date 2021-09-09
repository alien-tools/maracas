package main.unused.unstableAnnon.methodRemoved;

import main.unstableAnnon.IsUnstable;

public class MethodRemoved {

	@IsUnstable
	public int methodRemoved() {
		return 0;
	}
	
	public int methodStay() {
		return 1;
	}
}
