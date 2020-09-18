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

package com.hh.xml.internal.ws.transport.http;

import com.hh.xml.internal.ws.transport.http.DeploymentDescriptorParser.AdapterFactory;
import com.hh.xml.internal.ws.api.server.WSEndpoint;
import com.hh.xml.internal.ws.api.server.PortAddressResolver;
import com.hh.xml.internal.ws.api.model.wsdl.WSDLPort;
import com.sun.istack.internal.NotNull;

import javax.xml.namespace.QName;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.AbstractList;

/**
 * List of {@link HttpAdapter}s created together.
 *
 * <p>
 * Some cases WAR file may contain multiple endpoints for ports in a WSDL.
 * If the runtime knows these ports, their port addresses can be patched.
 * This class keeps a list of {@link HttpAdapter}s and use that information to patch
 * multiple port addresses.
 *
 * <p>
 * Concrete implementations of this class need to override {@link #createHttpAdapter}
 * method to create implementations of {@link HttpAdapter}.
 *
 * @author Jitendra Kotamraju
 */
public abstract class HttpAdapterList<T extends HttpAdapter> extends AbstractList<T> implements AdapterFactory<T> {
    private final List<T> adapters = new ArrayList<T>();
    private final Map<PortInfo, String> addressMap = new HashMap<PortInfo, String>();

    // TODO: documented because it's used by AS
    public T createAdapter(String name, String urlPattern, WSEndpoint<?> endpoint) {
        T t = createHttpAdapter(name, urlPattern, endpoint);
        adapters.add(t);
        WSDLPort port = endpoint.getPort();
        if (port != null) {
            PortInfo portInfo = new PortInfo(port.getOwner().getName(),port.getName().getLocalPart());
            addressMap.put(portInfo, getValidPath(urlPattern));
        }
        return t;
    }

    /**
     * Implementations need to override this one to create a concrete class
     * of HttpAdapter
     */
    protected abstract T createHttpAdapter(String name, String urlPattern, WSEndpoint<?> endpoint);

    /**
     * @return urlPattern without "/*"
     */
    private String getValidPath(@NotNull String urlPattern) {
        if (urlPattern.endsWith("/*")) {
            return urlPattern.substring(0, urlPattern.length() - 2);
        } else {
            return urlPattern;
        }
    }

    /**
     * Creates a PortAddressResolver that maps portname to its address
     */
    public PortAddressResolver createPortAddressResolver(final String baseAddress) {
        return new PortAddressResolver() {
            public String getAddressFor(@NotNull QName serviceName, @NotNull String portName) {
                String urlPattern = addressMap.get(new PortInfo(serviceName,portName));
                return (urlPattern == null) ? null : baseAddress+urlPattern;
            }
        };
    }


    public T get(int index) {
        return adapters.get(index);
    }

    public int size() {
        return adapters.size();
    }

    private static class PortInfo {
        private final QName serviceName;
        private final String portName;

        PortInfo(@NotNull QName serviceName, @NotNull String portName) {
            this.serviceName = serviceName;
            this.portName = portName;
        }

        @Override
        public boolean equals(Object portInfo) {
            if (portInfo instanceof PortInfo) {
                PortInfo that = (PortInfo)portInfo;
                return this.serviceName.equals(that.serviceName) && this.portName.equals(that.portName);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return serviceName.hashCode()+portName.hashCode();
        }
    }
}
