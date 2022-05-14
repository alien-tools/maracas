package genericsClazzConstructorTypeSwap;

import testing_lib.genericsClazzConstructorTypeSwap.GenericsClazzConstructorTypeSwap;

public class Main {

	public static void main(String[] args) {
		GenericsClazzConstructorTypeSwap constr =  new <Integer, String>GenericsClazzConstructorTypeSwap();
	}
	
}
