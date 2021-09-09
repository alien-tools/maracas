package main.methodNewDefault;

public interface IMethodNewDefaultOther {

	default int defaultMethod() {
		return 10;
	}
}
