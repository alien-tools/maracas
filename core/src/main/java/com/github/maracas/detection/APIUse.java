package com.github.maracas.detection;

/**
 * Represents how a particular element in source code is using a broken declaration.
 * <br>
 * e.g.:
 * <ul>
 *   <li>{@code obj.removedMethod()} uses {@code removedMethod()} through {@code METHOD_INVOCATION}</li>
 *   <li>{@code throw new RemovedException()} uses {@code RemovedException} through {@code THROWS}</li>
 *   <li>etc.</li>
 * </ul>
 */
public enum APIUse {
	THROWS,
	INSTANTIATION,
	METHOD_INVOCATION,
	METHOD_OVERRIDE,
	FIELD_ACCESS,
	EXTENDS,
	IMPLEMENTS,
	ANNOTATION,
	TYPE_DEPENDENCY,
	DECLARATION,
	IMPORT
}
