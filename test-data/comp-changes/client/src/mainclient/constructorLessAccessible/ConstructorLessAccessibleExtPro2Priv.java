package mainclient.constructorLessAccessible;

import main.constructorLessAccessible.ConstructorLessAccessiblePro2Priv;

public class ConstructorLessAccessibleExtPro2Priv extends ConstructorLessAccessiblePro2Priv {

	public ConstructorLessAccessibleExtPro2Priv() {
		super();
	}
	
	public ConstructorLessAccessibleExtPro2Priv(int i) {
		super(i);
	}
	
}
