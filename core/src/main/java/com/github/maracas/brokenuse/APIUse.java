package com.github.maracas.brokenuse;

/**
 * Represents how a particular element in client source code uses a declaration impacted by a breaking change, e.g.:
 * <ul>
 *   <li>{@code obj.removedMethod()} uses {@code removedMethod()} through {@code METHOD_INVOCATION}</li>
 *   <li>{@code throw new RemovedException()} uses {@code RemovedException} through {@code THROWS}</li>
 *   <li>etc.</li>
 * </ul>
 */
public enum APIUse {
	ANNOTATION,
	EXTENDS,
	FIELD_ACCESS,
	IMPLEMENTS,
	IMPORT,
	INSTANTIATION,
	METHOD_INVOCATION,
	METHOD_OVERRIDE,
	THROWS,
	TYPE_DEPENDENCY
}
