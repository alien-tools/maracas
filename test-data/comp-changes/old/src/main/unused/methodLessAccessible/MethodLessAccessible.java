package main.unused.methodLessAccessible;

public class MethodLessAccessible {
	
	public int methodLessAccessiblePublic2Protected() {
		return 0;
	}
	
	public int methodLessAccessiblePublic2PackPriv() {
		return 1;
	}
	
	public int methodLessAccessiblePublic2Private() {
		return 2;
	}
	
	protected int methodLessAccessibleProtected2Public() {
		return 3;
	}
	
	protected int methodLessAccessibleProtected2PackPriv() {
		return 4;
	}
	
	protected int methodLessAccessibleProtected2Private() {
		return 5;
	}
	
	int methodLessAccessiblePackPriv2Public() {
		return 6;
	}
	
	int methodLessAccessiblePackPriv2Protected() {
		return 7;
	}
	
	int methodLessAccessiblePackPriv2Private() {
		return 8;
	}
	
	private int methodLessAccessiblePrivate2Public() {
		return 9;
	}
	
	private int methodLessAccessiblePrivate2PackPriv() {
		return 10;
	}
	
	private int methodLessAccessiblePrivate2Protected() {
		return 11;
	}
}
