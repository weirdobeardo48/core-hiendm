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

package com.hh.xml.internal.ws.server;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import com.hh.xml.internal.ws.api.message.Packet;
import com.hh.xml.internal.ws.api.pipe.TubeCloner;
import com.hh.xml.internal.ws.api.pipe.helper.AbstractTubeImpl;
import com.hh.xml.internal.ws.api.server.AsyncProviderCallback;
import com.hh.xml.internal.ws.api.server.InstanceResolver;
import com.hh.xml.internal.ws.api.server.Invoker;
import com.hh.xml.internal.ws.api.server.WSEndpoint;
import com.hh.xml.internal.ws.api.server.WSWebServiceContext;
import com.hh.xml.internal.ws.resources.ServerMessages;
import com.hh.xml.internal.ws.server.provider.ProviderInvokerTube;
import com.hh.xml.internal.ws.server.sei.SEIInvokerTube;

import com.hh.webservice.ws.WebServiceContext;
import com.hh.webservice.ws.WebServiceException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Base code for {@link ProviderInvokerTube} and {@link SEIInvokerTube}.
 *
 * <p>
 * This hides {@link InstanceResolver} and performs a set up
 * necessary for {@link WebServiceContext} to correctly.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class InvokerTube<T> extends AbstractTubeImpl {

    private final Invoker invoker;
    private WSEndpoint endpoint;

    protected InvokerTube(Invoker invoker) {
        this.invoker = invoker;
    }

    public void setEndpoint(WSEndpoint endpoint) {
        this.endpoint = endpoint;
        WSWebServiceContext webServiceContext = new AbstractWebServiceContext(endpoint) {
            public @Nullable Packet getRequestPacket() {
                Packet p = packets.get();
                return p;
            }
        };
        invoker.start(webServiceContext,endpoint);
    }

    protected WSEndpoint getEndpoint() {
        return endpoint;
    }

    /**
     * Returns the application object that serves the request.
     *
    public final @NotNull T getServant(Packet request) {
        // this allows WebServiceContext to find this packet
        packets.set(request);
        return invoker.resolve(request);
    }
     */

    /**
     * Returns the {@link Invoker} object that serves the request.
     */
    public final @NotNull Invoker getInvoker(Packet request) {
        return wrapper;
    }

    /**
     * processRequest() and processResponse() do not share any instance variables
     * while processing the request. {@link InvokerTube} is stateless and terminal,
     * so no need to create copies.
     */
    public final AbstractTubeImpl copy(TubeCloner cloner) {
        cloner.add(this,this);
        return this;
    }

    public void preDestroy() {
        invoker.dispose();
    }

    /**
     * Heart of {@link WebServiceContext}.
     * Remembers which thread is serving which packet.
     */
    private static final ThreadLocal<Packet> packets = new ThreadLocal<Packet>();

    /**
     * This method can be called while the user service is servicing the request
     * synchronously, to obtain the current request packet.
     *
     * <p>
     * This is primarily designed for {@link StatefulInstanceResolver}. Use with care.
     */
    public static @NotNull Packet getCurrentPacket() {
        Packet packet = packets.get();
        if(packet==null)
            throw new WebServiceException(ServerMessages.NO_CURRENT_PACKET());
        return packet;
    }

    /**
     * {@link Invoker} filter that sets and restores the current packet.
     */
    private final Invoker wrapper = new Invoker() {
        @Override
        public Object invoke(Packet p, Method m, Object... args) throws InvocationTargetException, IllegalAccessException {
            Packet old = set(p);
            try {
                return invoker.invoke(p, m, args);
            } finally {
                set(old);
            }
        }

        @Override
        public <T>T invokeProvider(Packet p, T arg) throws IllegalAccessException, InvocationTargetException {
            Packet old = set(p);
            try {
                return invoker.invokeProvider(p, arg);
            } finally {
                set(old);
            }
        }

        @Override
        public <T>void invokeAsyncProvider(Packet p, T arg, AsyncProviderCallback cbak, WebServiceContext ctxt) throws IllegalAccessException, InvocationTargetException {
            Packet old = set(p);
            try {
                invoker.invokeAsyncProvider(p, arg, cbak, ctxt);
            } finally {
                set(old);
            }
        }

        private Packet set(Packet p) {
            Packet old = packets.get();
            packets.set(p);
            return old;
        }
    };

}
