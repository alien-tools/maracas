package main.classLessAccessible;

public interface IClassLessAccessiblePub2PackPriv {
	public static final int publicField = 1;;
	
	public static int publicMethod() {
		return 0;
	}
	
	public static int privateMethod() {
		return 0;
	}
}
