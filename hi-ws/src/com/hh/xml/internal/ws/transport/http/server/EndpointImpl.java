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

package com.hh.xml.internal.ws.transport.http.server;

import com.hh.webservice.ws.Binding;
import com.hh.webservice.ws.Endpoint;
import com.hh.webservice.ws.EndpointContext;
import com.hh.webservice.ws.EndpointReference;
import com.hh.webservice.ws.WebServiceException;
import com.hh.webservice.ws.WebServiceFeature;
import com.hh.webservice.ws.WebServicePermission;
import com.hh.webservice.ws.spi.http.HttpContext;
import com.hh.webservice.ws.wsaddressing.W3CEndpointReference;
import com.sun.istack.internal.Nullable;
import com.hh.xml.internal.stream.buffer.XMLStreamBufferResult;
import com.hh.xml.internal.ws.api.WSBinding;
import com.hh.xml.internal.ws.api.BindingID;
import com.hh.xml.internal.ws.api.message.Packet;
import com.hh.xml.internal.ws.api.server.Container;
import com.hh.xml.internal.ws.api.server.InstanceResolver;
import com.hh.xml.internal.ws.api.server.Invoker;
import com.hh.xml.internal.ws.api.server.SDDocumentSource;
import com.hh.xml.internal.ws.api.server.WSEndpoint;
import com.hh.xml.internal.ws.api.server.WSWebServiceContext;
import com.hh.xml.internal.ws.binding.BindingImpl;
import com.hh.xml.internal.ws.server.EndpointFactory;
import com.hh.xml.internal.ws.server.ServerRtException;
import com.hh.xml.internal.ws.util.xml.XmlUtil;
import com.hh.xml.internal.ws.transport.http.HttpAdapterList;
import com.hh.xml.internal.ws.transport.http.HttpAdapter;
import com.sun.istack.internal.NotNull;

import java.net.MalformedURLException;



import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;

import org.xml.sax.EntityResolver;
import org.xml.sax.SAXException;
import org.w3c.dom.Element;


/**
 * Implements {@link Endpoint}.
 * <p/>
 * <p/>
 * This class accumulates the information necessary to create
 * {@link WSEndpoint}, and then when {@link #publish} method
 * is called it will be created.
 * <p/>
 * <p/>
 * This object also allows accumulated information to be retrieved.
 *
 * @author Jitendra Kotamraju
 */
public class EndpointImpl extends Endpoint {

    private static final WebServicePermission ENDPOINT_PUBLISH_PERMISSION =
            new WebServicePermission("publishEndpoint");

    /**
     * Once the service is published, this field will
     * be set to the {@link HttpEndpoint} instance.
     * <p/>
     * But don't declare the type as {@link HttpEndpoint}
     * to avoid static type dependency that cause the class loading to
     * fail if the LW HTTP server doesn't exist.
     */
    private Object actualEndpoint;

    // information accumulated for creating WSEndpoint
    private final WSBinding binding;
    private @Nullable final Object implementor;
    private List<Source> metadata;
    private Executor executor;
    private Map<String, Object> properties = Collections.emptyMap(); // always non-null
    private boolean stopped;
    private @Nullable EndpointContext endpointContext;
    private @NotNull final Class<?> implClass;
    private final Invoker invoker;
    private Container container;


    public EndpointImpl(@NotNull BindingID bindingId, @NotNull Object impl,
                        WebServiceFeature ... features) {
        this(bindingId, impl, impl.getClass(),
             InstanceResolver.createSingleton(impl).createInvoker(),  features);
    }

    public EndpointImpl(@NotNull BindingID bindingId, @NotNull Class implClass,
                        com.hh.webservice.ws.spi.Invoker invoker,
                        WebServiceFeature ... features) {
        this(bindingId, null, implClass, new InvokerImpl(invoker),  features);
    }

    private EndpointImpl(@NotNull BindingID bindingId, Object impl, @NotNull Class implClass,
                        Invoker invoker, WebServiceFeature ... features) {
        binding = BindingImpl.create(bindingId, features);
        this.implClass = implClass;
        this.invoker = invoker;
        this.implementor = impl;
    }


    /**
     * Wraps an already created {@link WSEndpoint} into an {@link EndpointImpl},
     * and immediately publishes it with the given context.
     *
     * @param wse created endpoint
     * @param serverContext supported http context
     * @deprecated This is a backdoor method. Don't use it unless you know what you are doing.
     */
    public EndpointImpl(WSEndpoint wse, Object serverContext) {
        actualEndpoint = new HttpEndpoint(null, getAdapter(wse, ""));
        ((HttpEndpoint) actualEndpoint).publish(serverContext);
        binding = wse.getBinding();
        implementor = null; // this violates the semantics, but hey, this is a backdoor.
        implClass = null;
        invoker = null;
    }

    public Binding getBinding() {
        return binding;
    }

    public Object getImplementor() {
        return implementor;
    }

    public void publish(String address) {
        canPublish();
        URL url;
        try {
            url = new URL(address);
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException("Cannot create URL for this address " + address);
        }
        if (!url.getProtocol().equals("http")) {
            throw new IllegalArgumentException(url.getProtocol() + " protocol based address is not supported");
        }
        if (!url.getPath().startsWith("/")) {
            throw new IllegalArgumentException("Incorrect WebService address=" + address +
                    ". The address's path should start with /");
        }
        createEndpoint(url.getPath());
        ((HttpEndpoint) actualEndpoint).publish(address);
    }

    public void publish(Object serverContext) {
        canPublish();
        if (!com.hh.net.httpserver.HttpContext.class.isAssignableFrom(serverContext.getClass())) {
            throw new IllegalArgumentException(serverContext.getClass() + " is not a supported context.");
        }
        createEndpoint(((com.hh.net.httpserver.HttpContext)serverContext).getPath());
        ((HttpEndpoint) actualEndpoint).publish(serverContext);
    }

    public void publish(HttpContext serverContext) {
        canPublish();
        createEndpoint(serverContext.getPath());
        ((HttpEndpoint) actualEndpoint).publish(serverContext);
    }

    public void stop() {
        if (isPublished()) {
            ((HttpEndpoint) actualEndpoint).stop();
            actualEndpoint = null;
            stopped = true;
        }
    }

    public boolean isPublished() {
        return actualEndpoint != null;
    }

    public List<Source> getMetadata() {
        return metadata;
    }

    public void setMetadata(java.util.List<Source> metadata) {
        if (isPublished()) {
            throw new IllegalStateException("Cannot set Metadata. Endpoint is already published");
        }
        this.metadata = metadata;
    }

    public Executor getExecutor() {
        return executor;
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    public Map<String, Object> getProperties() {
        return new HashMap<String, Object>(properties);
    }

    public void setProperties(Map<String, Object> map) {
        this.properties = new HashMap<String, Object>(map);
    }

    /*
    * Checks the permission of "publishEndpoint" before accessing HTTP classes.
    * Also it checks if there is an available HTTP server implementation.
    */
    private void createEndpoint(String urlPattern) {
        // Checks permission for "publishEndpoint"
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(ENDPOINT_PUBLISH_PERMISSION);
        }

        // See if HttpServer implementation is available
        try {
            Class.forName("com.sun.net.httpserver.HttpServer");
        } catch (Exception e) {
            throw new UnsupportedOperationException("Couldn't load light weight http server", e);
        }
        container = getContainer();
        WSEndpoint wse = WSEndpoint.create(
                implClass, true,
                invoker,
                getProperty(QName.class, Endpoint.WSDL_SERVICE),
                getProperty(QName.class, Endpoint.WSDL_PORT),
                container,
                binding,
                getPrimaryWsdl(),
                buildDocList(),
                (EntityResolver) null,
                false
        );
        // Don't load HttpEndpoint class before as it may load HttpServer classes
        actualEndpoint = new HttpEndpoint(executor, getAdapter(wse, urlPattern));
    }

    private <T> T getProperty(Class<T> type, String key) {
        Object o = properties.get(key);
        if (o == null) return null;
        if (type.isInstance(o))
            return type.cast(o);
        else
            throw new IllegalArgumentException("Property " + key + " has to be of type " + type);   // i18n
    }

    /**
     * Convert metadata sources using identity transform. So that we can
     * reuse the Source object multiple times.
     */
    private List<SDDocumentSource> buildDocList() {
        List<SDDocumentSource> r = new ArrayList<SDDocumentSource>();

        if (metadata != null) {
            for (Source source : metadata) {
                try {
                    XMLStreamBufferResult xsbr = XmlUtil.identityTransform(source, new XMLStreamBufferResult());
                    String systemId = source.getSystemId();

                    r.add(SDDocumentSource.create(new URL(systemId), xsbr.getXMLStreamBuffer()));
                } catch (TransformerException te) {
                    throw new ServerRtException("server.rt.err", te);
                } catch (IOException te) {
                    throw new ServerRtException("server.rt.err", te);
                } catch (SAXException e) {
                    throw new ServerRtException("server.rt.err", e);
                } catch (ParserConfigurationException e) {
                    throw new ServerRtException("server.rt.err", e);
                }
            }
        }

        return r;
    }

    /**
     * Gets wsdl from @WebService or @WebServiceProvider
     */
    private @Nullable SDDocumentSource getPrimaryWsdl() {
        // Takes care of @WebService, @WebServiceProvider's wsdlLocation
        EndpointFactory.verifyImplementorClass(implClass);
        String wsdlLocation = EndpointFactory.getWsdlLocation(implClass);
        if (wsdlLocation != null) {
            ClassLoader cl = implClass.getClassLoader();
            URL url = cl.getResource(wsdlLocation);
            if (url != null) {
                return SDDocumentSource.create(url);
            }
            throw new ServerRtException("cannot.load.wsdl", wsdlLocation);
        }
        return null;
    }

    private void canPublish() {
        if (isPublished()) {
            throw new IllegalStateException(
                    "Cannot publish this endpoint. Endpoint has been already published.");
        }
        if (stopped) {
            throw new IllegalStateException(
                    "Cannot publish this endpoint. Endpoint has been already stopped.");
        }
    }

    public EndpointReference getEndpointReference(Element...referenceParameters) {
        return getEndpointReference(W3CEndpointReference.class, referenceParameters);
    }

    public <T extends EndpointReference> T getEndpointReference(Class<T> clazz, Element...referenceParameters) {
        if (!isPublished()) {
            throw new WebServiceException("Endpoint is not published yet");
        }
        return ((HttpEndpoint)actualEndpoint).getEndpointReference(clazz,referenceParameters);
    }

    @Override
    public void setEndpointContext(EndpointContext ctxt) {
        this.endpointContext = ctxt;
    }

    private HttpAdapter getAdapter(WSEndpoint endpoint, String urlPattern) {
        HttpAdapterList adapterList = null;
        if (endpointContext != null) {
            for(Endpoint e : endpointContext.getEndpoints()) {
                if (e.isPublished() && e != this) {
                    adapterList = ((HttpEndpoint)(((EndpointImpl)e).actualEndpoint)).getAdapterOwner();
                    assert adapterList != null;
                    break;
                }
            }
        }
        if (adapterList == null) {
            adapterList = new ServerAdapterList();
        }
        return adapterList.createAdapter("", urlPattern, endpoint);
    }

    /**
     * Endpoints within a EndpointContext get the same container.
     */
    private Container getContainer() {
        if (endpointContext != null) {
            for(Endpoint e : endpointContext.getEndpoints()) {
                if (e.isPublished() && e != this) {
                    return ((EndpointImpl)e).container;
                }
            }
        }
        return new ServerContainer();
    }

    private static class InvokerImpl extends Invoker {
        private com.hh.webservice.ws.spi.Invoker spiInvoker;

        InvokerImpl(com.hh.webservice.ws.spi.Invoker spiInvoker) {
            this.spiInvoker = spiInvoker;
        }

        @Override
        public void start(@NotNull WSWebServiceContext wsc, @NotNull WSEndpoint endpoint) {
            try {
                spiInvoker.inject(wsc);
            } catch (IllegalAccessException e) {
                throw new WebServiceException(e);
            } catch (InvocationTargetException e) {
                throw new WebServiceException(e);
            }
        }

        public Object invoke(@NotNull Packet p, @NotNull Method m, @NotNull Object... args) throws InvocationTargetException, IllegalAccessException {
            return spiInvoker.invoke(m, args);
        }
    }
}
