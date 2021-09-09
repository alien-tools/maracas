package mainclient.constructorLessAccessible;

import main.constructorLessAccessible.ConstructorLessAccessiblePub2Priv;

public class ConstructorLessAccessibleExtPub2Priv extends ConstructorLessAccessiblePub2Priv {

	public ConstructorLessAccessibleExtPub2Priv() {
		super();
	}
	
	public ConstructorLessAccessibleExtPub2Priv(int i) {
		super(i);
	}
}
