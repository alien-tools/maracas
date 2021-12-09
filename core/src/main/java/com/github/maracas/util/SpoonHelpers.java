package com.github.maracas.util;

import javassist.CtBehavior;
import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtLoop;
import spoon.reflect.code.CtThrow;
import spoon.reflect.cu.position.NoSourcePosition;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.declaration.CtTypedElement;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtReference;
import spoon.reflect.reference.CtTypeReference;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.List;

public class SpoonHelpers {

	private SpoonHelpers() {}

	public static CtModel buildSpoonModel(Path sources, Path cp) {
		// Spoon will prioritize the JVM's classpath over our own
		// custom classpath in case of conflict. Not what we want,
		// so we use a custom child-first classloader instead.
		// cf. https://github.com/INRIA/spoon/issues/3789
		Launcher launcher = new Launcher();
		//String[] javaCp = { cp.toAbsolutePath().toString() };
		//launcher.getEnvironment().setSourceClasspath(javaCp);
		ClassLoader cl = new ParentLastURLClassLoader(List.of("file:" + cp.toAbsolutePath().toString()));
		launcher.getEnvironment().setInputClassLoader(cl);

		if (sources != null)
			launcher.addInputResource(sources.toAbsolutePath().toString());

		return launcher.buildModel();
	}

	public static CtElement firstLocatableParent(CtElement element) {
		CtElement parent = element;
		do {
			if (!(parent.getPosition() instanceof NoSourcePosition))
				return parent;
		} while ((parent = parent.getParent()) != null);
		return parent;
	}

	/**
	 * Verifies if the signature of a Spoon method (CtExecutableReference)
	 * is equivalent to the one of the JApiCmp method (CtBehavior).
	 * @param spoonMethod the Spoon method
	 * @param japiMethod  The JapiCmp method
	 * @return            <code>true</code> if the methods have the same
	 *                    signature; <code>false</code> otherwise.
	 */
	public static boolean matchingSignatures(CtExecutableReference<?> spoonMethod, CtBehavior japiMethod) {
		String japiMethName = "";

		if (spoonMethod.isConstructor() && japiMethod.getLongName().contains("$")) {  // Inner class constructor
			String ln = japiMethod.getLongName();
			String outerCN = ln.substring(0, ln.indexOf("$"));
			japiMethName = ln.replaceAll(String.format("\\(%s,?", outerCN), "(");
		} else if (spoonMethod.isConstructor()) {                                     // Regular constructor
			japiMethName = japiMethod.getLongName();
		} else {                                                                      // Regular method
			japiMethName = japiMethod.getName().concat(japiMethod.getSignature());
		}

		return japiMethName.startsWith(spoonMethod.getSignature());
	}

	public static String fullyQualifiedName(CtReference ref) {
		String fqn = "";
		if (ref instanceof CtTypeReference<?> tRef)
			fqn = tRef.getQualifiedName();
		else if (ref instanceof CtExecutableReference<?> eRef)
			fqn = eRef.getDeclaringType().getQualifiedName().concat(".").concat(eRef.getSignature());
		else if (ref instanceof CtFieldReference<?> fRef)
			fqn = fRef.getDeclaringType().getQualifiedName().concat(".").concat(fRef.getSimpleName());

		return fqn;
	}

	public static String getEnclosingPkgName(CtElement e) {
		CtPackage enclosing = e.getParent(CtPackage.class);
		return
			enclosing != null ?
				enclosing.getQualifiedName() :
				CtPackage.TOP_LEVEL_PACKAGE_NAME;
	}

	// Oof
	public static CtTypeReference<?> inferExpectedType(CtElement e) {
		if (e instanceof CtTypedElement<?> elem)
			return elem.getType();
		else if (e instanceof CtLoop)
			return e.getFactory().Type().booleanPrimitiveType();
		else if (e instanceof CtIf)
			return e.getFactory().Type().booleanPrimitiveType();
		else if (e instanceof CtThrow thrw)
			return thrw.getThrownExpression().getType();

		// FIXME: CtSwitch not supported yet

		throw new RuntimeException("Unhandled enclosing type " + e.getClass());
	}

	/**
	 * cf https://stackoverflow.com/a/5446671
	 *
	 * A parent-last classloader that will try the child classloader first and then the parent.
	 * This takes a fair bit of doing because java really prefers parent-first.
	 *
	 * For those not familiar with class loading trickery, be wary
	 */
	private static class ParentLastURLClassLoader extends ClassLoader {
		private ChildURLClassLoader childClassLoader;

		/**
		 * This class allows me to call findClass on a classloader
		 */
		private static class FindClassClassLoader extends ClassLoader {
			public FindClassClassLoader(ClassLoader parent) {
				super(parent);
			}

			@Override
			public Class<?> findClass(String name) throws ClassNotFoundException {
				return super.findClass(name);
			}
		}

		/**
		 * This class delegates (child then parent) for the findClass method for a URLClassLoader.
		 * We need this because findClass is protected in URLClassLoader
		 */
		private static class ChildURLClassLoader extends URLClassLoader {
			private FindClassClassLoader realParent;

			public ChildURLClassLoader(URL[] urls, FindClassClassLoader realParent) {
				super(urls, null);
				this.realParent = realParent;
			}

			@Override
			public Class<?> findClass(String name) throws ClassNotFoundException {
				Class<?> loaded = super.findLoadedClass(name);
				if (loaded != null)
					return loaded;

				try {
					// first try to use the URLClassLoader findClass
					return super.findClass(name);
				} catch (ClassNotFoundException e) {
					// if that fails, we ask our real parent classloader to load the class (we give up)
					return realParent.loadClass(name);
				}
			}
		}

		public ParentLastURLClassLoader(List<String> classpath) {
			super(Thread.currentThread().getContextClassLoader());

			URL[] urls = new URL[classpath.size()];
			for (int i = 0; i < classpath.size(); i++)
				try {
					urls[i] = new URL(classpath.get(i));
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}

			childClassLoader = new ChildURLClassLoader(urls, new FindClassClassLoader(this.getParent()));
		}

		@Override
		protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
			try {
				// first we try to find a class inside the child classloader
				return childClassLoader.findClass(name);
			} catch (ClassNotFoundException e) {
				// didn't find it, try the parent
				return super.loadClass(name, resolve);
			}
		}
	}
}
