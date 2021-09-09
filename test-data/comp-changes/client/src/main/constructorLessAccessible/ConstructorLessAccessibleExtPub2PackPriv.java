package main.constructorLessAccessible;

import main.constructorLessAccessible.ConstructorLessAccessiblePub2PackPriv;

public class ConstructorLessAccessibleExtPub2PackPriv extends ConstructorLessAccessiblePub2PackPriv {

	public ConstructorLessAccessibleExtPub2PackPriv() {
		super();
	}
	
	public ConstructorLessAccessibleExtPub2PackPriv(int i) {
		super(i);
	}
	
}
