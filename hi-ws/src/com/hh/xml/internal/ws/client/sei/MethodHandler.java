/*
 * Copyright (c) 1997, 2010, Oracle and/or its affiliates. All rights reserved.
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

package com.hh.xml.internal.ws.client.sei;

import com.hh.webservice.ws.WebServiceException;

/**
 * Handles an invocation of a method.
 *
 * <p>
 * Each instance of {@link MethodHandler} has an implicit knowledge of
 * a particular method that it handles.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class MethodHandler {

    protected final SEIStub owner;

    protected MethodHandler(SEIStub owner) {
        this.owner = owner;
    }

    /**
     * Performs the method invocation.
     *
     * @param proxy
     *      The proxy object exposed to the user. Must not be null.
     * @param args
     *      The method invocation arguments. To handle asynchroonus method invocations
     *      without array reallocation, this aray is allowed to be longer than the
     *      actual number of arguments to the method. Additional array space should be
     *      simply ignored.
     * @return
     *      a return value from the method invocation. may be null.
     *
     * @throws WebServiceException
     *      If used on the client side, a {@link WebServiceException} signals an error
     *      during the service invocation.
     * @throws Throwable
     *      some faults are reported in terms of checked exceptions.
     */
    abstract Object invoke(Object proxy, Object[] args) throws WebServiceException, Throwable;
}
