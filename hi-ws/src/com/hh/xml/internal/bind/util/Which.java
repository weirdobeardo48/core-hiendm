/*
 * Copyright (c) 1997, 2011, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package com.hh.xml.internal.bind.util;

import java.net.URL;

/**
 * Finds out where a class file is loaded from.
 *
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class Which {

    public static String which( Class clazz ) {
        return which( clazz.getName(), clazz.getClassLoader() );
    }

    /**
     * Search the specified classloader for the given classname.
     *
     * @param classname the fully qualified name of the class to search for
     * @param loader the classloader to search
     * @return the source location of the resource, or null if it wasn't found
     */
    public static String which(String classname, ClassLoader loader) {

        String classnameAsResource = classname.replace('.', '/') + ".class";

        if(loader == null) {
            loader = ClassLoader.getSystemClassLoader();
        }

        URL it = loader.getResource(classnameAsResource);
        if (it != null) {
            return it.toString();
        } else {
            return null;
        }
    }

}
