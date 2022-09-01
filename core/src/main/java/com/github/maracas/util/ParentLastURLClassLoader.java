package com.github.maracas.util;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * cf <a href="https://stackoverflow.com/a/5446671">this SO question</a>
 * <p>
 * A parent-last classloader that will try the child classloader first and then the parent.
 * This takes a fair bit of doing because java really prefers parent-first.
 * <p>
 * For those not familiar with class loading trickery, be wary
 */
public class ParentLastURLClassLoader extends URLClassLoader {
	private final ChildURLClassLoader childClassLoader;

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
		private final FindClassClassLoader realParent;

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

	public ParentLastURLClassLoader(URL[] urls) {
		super(urls, Thread.currentThread().getContextClassLoader());
		childClassLoader = new ChildURLClassLoader(urls, new FindClassClassLoader(this.getParent()));
	}

	public ParentLastURLClassLoader(URL[] urls, ClassLoader parent) {
		super(urls, parent);
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
