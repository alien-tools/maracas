package mainclient.constructorRemoved;

import main.constructorRemoved.ConstructorRemovedParams;

public class ConstructorRemovedExtParams extends ConstructorRemovedParams {

	public ConstructorRemovedExtParams() {
		super(0);
	}
	
	public void constructorRemovedExtParamsNoSuper() {
		ConstructorRemovedExtParams c = (ConstructorRemovedExtParams) new ConstructorRemovedParams(0);
	}
}
