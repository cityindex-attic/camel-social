/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.social.providers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.ServiceConfigurationError;

/**
 * <p>
 * Hard copy of java.util.ServiceLoader contained on Java's SDK 6.0
 * </p>
 */
public final class ProviderLoader implements Iterable<SocialProvider> {

	private static final String PREFIX = "META-INF/services/";

	// The class or interface representing the service being loaded
	private Class<SocialProvider> service = SocialProvider.class;

	// The class loader used to locate, load, and instantiate providers
	private ClassLoader loader;

	// Cached providers, in instantiation order
	private LinkedHashMap<String, SocialProvider> providers = new LinkedHashMap<String, SocialProvider>();

	// The current lazy-lookup iterator
	private LazyIterator lookupIterator;

	/**
	 * Clear this loader's provider cache so that all providers will be
	 * reloaded.
	 * 
	 * <p>
	 * After invoking this method, subsequent invocations of the
	 * {@link #iterator() iterator} method will lazily look up and instantiate
	 * providers from scratch, just as is done by a newly-created loader.
	 * 
	 * <p>
	 * This method is intended for use in situations in which new providers can
	 * be installed into a running Java virtual machine.
	 */
	public void reload() {
		providers.clear();
		lookupIterator = new LazyIterator(service, loader);
	}

	private ProviderLoader() {
		loader = Thread.currentThread().getContextClassLoader();
		reload();
	}

	private static void fail(Class<?> service, String msg, Throwable cause) throws ServiceConfigurationError {
		throw new ServiceConfigurationError(service.getName() + ": " + msg, cause);
	}

	private static void fail(Class<?> service, String msg) throws ServiceConfigurationError {
		throw new ServiceConfigurationError(service.getName() + ": " + msg);
	}

	private static void fail(Class<?> service, URL u, int line, String msg) throws ServiceConfigurationError {
		fail(service, u + ":" + line + ": " + msg);
	}

	// Parse a single line from the given configuration file, adding the name
	// on the line to the names list.
	//
	private int parseLine(Class<?> service, URL u, BufferedReader r, int lc, List<String> names) throws IOException,
			ServiceConfigurationError {
		String ln = r.readLine();
		if (ln == null) {
			return -1;
		}
		int ci = ln.indexOf('#');
		if (ci >= 0)
			ln = ln.substring(0, ci);
		ln = ln.trim();
		int n = ln.length();
		if (n != 0) {
			if ((ln.indexOf(' ') >= 0) || (ln.indexOf('\t') >= 0))
				fail(service, u, lc, "Illegal configuration-file syntax");
			int cp = ln.codePointAt(0);
			if (!Character.isJavaIdentifierStart(cp))
				fail(service, u, lc, "Illegal provider-class name: " + ln);
			for (int i = Character.charCount(cp); i < n; i += Character.charCount(cp)) {
				cp = ln.codePointAt(i);
				if (!Character.isJavaIdentifierPart(cp) && (cp != '.'))
					fail(service, u, lc, "Illegal provider-class name: " + ln);
			}
			if (!providers.containsKey(ln) && !names.contains(ln))
				names.add(ln);
		}
		return lc + 1;
	}

	// Parse the content of the given URL as a provider-configuration file.
	//
	// @param service
	// The service type for which providers are being sought;
	// used to construct error detail strings
	//
	// @param u
	// The URL naming the configuration file to be parsed
	//
	// @return A (possibly empty) iterator that will yield the provider-class
	// names in the given configuration file that are not yet members
	// of the returned set
	//
	// @throws ServiceConfigurationError
	// If an I/O error occurs while reading from the given URL, or
	// if a configuration-file format error is detected
	//
	private Iterator<String> parse(Class<?> service, URL u) throws ServiceConfigurationError {
		InputStream in = null;
		BufferedReader r = null;
		ArrayList<String> names = new ArrayList<String>();
		try {
			in = u.openStream();
			r = new BufferedReader(new InputStreamReader(in, "utf-8"));
			int lc = 1;
			while ((lc = parseLine(service, u, r, lc, names)) >= 0)
				;
		} catch (IOException x) {
			fail(service, "Error reading configuration file", x);
		} finally {
			try {
				if (r != null)
					r.close();
				if (in != null)
					in.close();
			} catch (IOException y) {
				fail(service, "Error closing configuration file", y);
			}
		}
		return names.iterator();
	}

	// Private inner class implementing fully-lazy provider lookup
	//
	private class LazyIterator implements Iterator<SocialProvider> {

		Class<SocialProvider> service;
		ClassLoader loader;
		Enumeration<URL> configs = null;
		Iterator<String> pending = null;
		String nextName = null;

		private LazyIterator(Class<SocialProvider> service, ClassLoader loader) {
			this.service = service;
			this.loader = loader;
		}

		public boolean hasNext() {
			if (nextName != null) {
				return true;
			}
			if (configs == null) {
				try {
					String fullName = PREFIX + service.getName();
					if (loader == null)
						configs = ClassLoader.getSystemResources(fullName);
					else
						configs = loader.getResources(fullName);
				} catch (IOException x) {
					fail(service, "Error locating configuration files", x);
				}
			}
			while ((pending == null) || !pending.hasNext()) {
				if (!configs.hasMoreElements()) {
					return false;
				}
				pending = parse(service, configs.nextElement());
			}
			nextName = pending.next();
			return true;
		}

		public SocialProvider next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			String cn = nextName;
			nextName = null;
			try {
				SocialProvider p = service.cast(Class.forName(cn, true, loader).newInstance());
				providers.put(cn, p);
				return p;
			} catch (ClassNotFoundException x) {
				fail(service, "Provider " + cn + " not found");
			} catch (Throwable x) {
				fail(service, "Provider " + cn + " could not be instantiated: " + x, x);
			}
			throw new Error(); // This cannot happen
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}

	}

	/**
	 * Lazily loads the available providers of this loader's service.
	 * 
	 * <p>
	 * The iterator returned by this method first yields all of the elements of
	 * the provider cache, in instantiation order. It then lazily loads and
	 * instantiates any remaining providers, adding each one to the cache in
	 * turn.
	 * 
	 * <p>
	 * To achieve laziness the actual work of parsing the available
	 * provider-configuration files and instantiating providers must be done by
	 * the iterator itself. Its {@link java.util.Iterator#hasNext hasNext} and
	 * {@link java.util.Iterator#next next} methods can therefore throw a
	 * {@link ServiceConfigurationError} if a provider-configuration file
	 * violates the specified format, or if it names a provider class that
	 * cannot be found and instantiated, or if the result of instantiating the
	 * class is not assignable to the service type, or if any other kind of
	 * exception or error is thrown as the next provider is located and
	 * instantiated. To write robust code it is only necessary to catch
	 * {@link ServiceConfigurationError} when using a service iterator.
	 * 
	 * <p>
	 * If such an error is thrown then subsequent invocations of the iterator
	 * will make a best effort to locate and instantiate the next available
	 * provider, but in general such recovery cannot be guaranteed.
	 * 
	 * <blockquote style="font-size: smaller; line-height: 1.2"><span
	 * style="padding-right: 1em; font-weight: bold">Design Note</span> Throwing
	 * an error in these cases may seem extreme. The rationale for this behavior
	 * is that a malformed provider-configuration file, like a malformed class
	 * file, indicates a serious problem with the way the Java virtual machine
	 * is configured or is being used. As such it is preferable to throw an
	 * error rather than try to recover or, even worse, fail
	 * silently.</blockquote>
	 * 
	 * <p>
	 * The iterator returned by this method does not support removal. Invoking
	 * its {@link java.util.Iterator#remove() remove} method will cause an
	 * {@link UnsupportedOperationException} to be thrown.
	 * 
	 * @return An iterator that lazily loads providers for this loader's service
	 */
	public Iterator<SocialProvider> iterator() {
		return new Iterator<SocialProvider>() {

			Iterator<Map.Entry<String, SocialProvider>> knownProviders = providers.entrySet().iterator();

			public boolean hasNext() {
				if (knownProviders.hasNext())
					return true;
				return lookupIterator.hasNext();
			}

			public SocialProvider next() {
				if (knownProviders.hasNext())
					return knownProviders.next().getValue();
				return lookupIterator.next();
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}

		};
	}

	/**
	 * Returns a socialEndpoint describing this service.
	 * 
	 * @return A descriptive socialEndpoint
	 */
	public String toString() {
		return "org.apache.camel.component.social.providers.ProviderLoader[" + service.getName() + "]";
	}

	public static ProviderLoader load() {
		return new ProviderLoader();
	}

}
