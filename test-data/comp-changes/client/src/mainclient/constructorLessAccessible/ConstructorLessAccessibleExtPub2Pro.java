package mainclient.constructorLessAccessible;

import main.constructorLessAccessible.ConstructorLessAccessiblePub2Pro;

public class ConstructorLessAccessibleExtPub2Pro extends ConstructorLessAccessiblePub2Pro {

	public ConstructorLessAccessibleExtPub2Pro() {
		super();
	}
	
	public ConstructorLessAccessibleExtPub2Pro(int i) {
		super(i);
	}
}
