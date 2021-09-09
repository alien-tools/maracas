package mainclient.constructorRemoved;

import main.constructorRemoved.ConstructorRemovedNoParams;
import main.constructorRemoved.ConstructorRemovedParams;

public class ConstructorRemovedMI {

	public void constructorRemovedClientNoParams() {
		ConstructorRemovedNoParams c = new ConstructorRemovedNoParams();
	}

	public void constructorRemovedClientParams() {
		ConstructorRemovedParams c = new ConstructorRemovedParams(0);
	}

	public void constructorRemovedClientNoParamsAnonymous() {
		ConstructorRemovedNoParams c = new ConstructorRemovedNoParams() {};
	}

	public void constructorRemovedClientParamsAnonymous() {
		ConstructorRemovedParams c = new ConstructorRemovedParams(0) {};
	}
}
