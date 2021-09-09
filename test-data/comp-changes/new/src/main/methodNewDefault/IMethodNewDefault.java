package main.methodNewDefault;

public interface IMethodNewDefault {

	default int defaultMethod() {
		return 0;
	}
}
