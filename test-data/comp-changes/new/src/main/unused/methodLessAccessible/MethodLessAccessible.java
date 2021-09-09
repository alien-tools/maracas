package main.unused.methodLessAccessible;

public class MethodLessAccessible {
	
	protected int methodLessAccessiblePublic2Protected() {
		return 0;
	}
	
	int methodLessAccessiblePublic2PackPriv() {
		return 1;
	}
	
	private int methodLessAccessiblePublic2Private() {
		return 2;
	}
	
	public int methodLessAccessibleProtected2Public() {
		return 3;
	}
	
	int methodLessAccessibleProtected2PackPriv() {
		return 4;
	}
	
	private int methodLessAccessibleProtected2Private() {
		return 5;
	}
	
	public int methodLessAccessiblePackPriv2Public() {
		return 6;
	}
	
	protected int methodLessAccessiblePackPriv2Protected() {
		return 7;
	}
	
	private int methodLessAccessiblePackPriv2Private() {
		return 8;
	}
	
	public int methodLessAccessiblePrivate2Public() {
		return 9;
	}
	
	int methodLessAccessiblePrivate2PackPriv() {
		return 10;
	}
	
	protected int methodLessAccessiblePrivate2Protected() {
		return 11;
	}
}
