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

package com.hh.xml.internal.ws.handler;

import com.hh.xml.internal.ws.api.WSBinding;
import com.hh.xml.internal.ws.api.message.Packet;
import com.hh.xml.internal.ws.api.message.Attachment;
import com.hh.xml.internal.ws.api.message.AttachmentSet;
import com.hh.xml.internal.ws.api.pipe.TubeCloner;
import com.hh.xml.internal.ws.api.pipe.Tube;
import com.hh.xml.internal.ws.api.pipe.helper.AbstractFilterTubeImpl;
import com.hh.xml.internal.ws.api.model.wsdl.WSDLPort;
import com.hh.xml.internal.ws.client.HandlerConfiguration;
import com.hh.xml.internal.ws.binding.BindingImpl;
import com.hh.xml.internal.ws.message.DataHandlerAttachment;

import com.hh.webservice.ws.handler.soap.SOAPHandler;
import com.hh.webservice.ws.handler.MessageContext;
import com.hh.webservice.ws.handler.Handler;
import com.hh.webservice.ws.WebServiceException;
import javax.activation.DataHandler;
import java.util.*;

/**
 *
 * @author WS Development Team
 */
public class ClientSOAPHandlerTube extends HandlerTube {

    private WSBinding binding;
    private Set<String> roles;

    /**
     * Creates a new instance of SOAPHandlerTube
     */
    public ClientSOAPHandlerTube(WSBinding binding, WSDLPort port, Tube next) {
        super(next, port);
        if (binding.getSOAPVersion() != null) {
            // SOAPHandlerTube should n't be used for bindings other than SOAP.
            // TODO: throw Exception
        }
        this.binding = binding;
    }

    // Handle to LogicalHandlerTube means its used on SERVER-SIDE

    /**
     * This constructor is used on client-side where, LogicalHandlerTube is created
     * first and then a SOAPHandlerTube is created with a handler to that
     * LogicalHandlerTube.
     * With this handle, SOAPHandlerTube can call LogicalHandlerTube.closeHandlers()
     */
    public ClientSOAPHandlerTube(WSBinding binding, Tube next, HandlerTube cousinTube) {
        super(next, cousinTube);
        this.binding = binding;
    }

    /**
     * Copy constructor for {@link com.sun.xml.internal.ws.api.pipe.Tube#copy(com.sun.xml.internal.ws.api.pipe.TubeCloner)}.
     */
    private ClientSOAPHandlerTube(ClientSOAPHandlerTube that, TubeCloner cloner) {
        super(that, cloner);
        this.binding = that.binding;
    }

   public AbstractFilterTubeImpl copy(TubeCloner cloner) {
        return new ClientSOAPHandlerTube(this, cloner);
    }

    void setUpProcessor() {
        // Take a snapshot, User may change chain after invocation, Same chain
        // should be used for the entire MEP
        handlers = new ArrayList<Handler>();
        HandlerConfiguration handlerConfig = ((BindingImpl) binding).getHandlerConfig();
        List<SOAPHandler> soapSnapShot= handlerConfig.getSoapHandlers();
        if (!soapSnapShot.isEmpty()) {
            handlers.addAll(soapSnapShot);
            roles = new HashSet<String>();
            roles.addAll(handlerConfig.getRoles());
            processor = new SOAPHandlerProcessor(true, this, binding, handlers);
        }
    }

    MessageUpdatableContext getContext(Packet packet) {
        SOAPMessageContextImpl context = new SOAPMessageContextImpl(binding, packet,roles);
        return context;
    }

    boolean callHandlersOnRequest(MessageUpdatableContext context, boolean isOneWay) {

        boolean handlerResult;
        //Lets copy all the MessageContext.OUTBOUND_ATTACHMENT_PROPERTY to the message
        Map<String, DataHandler> atts = (Map<String, DataHandler>) context.get(MessageContext.OUTBOUND_MESSAGE_ATTACHMENTS);
        AttachmentSet attSet = packet.getMessage().getAttachments();
        for(String cid : atts.keySet()){
            if (attSet.get(cid) == null) {  // Otherwise we would be adding attachments twice
                Attachment att = new DataHandlerAttachment(cid, atts.get(cid));
                attSet.add(att);
            }
        }

        try {
            //CLIENT-SIDE
            handlerResult = processor.callHandlersRequest(HandlerProcessor.Direction.OUTBOUND, context, !isOneWay);
        } catch (WebServiceException wse) {
            remedyActionTaken = true;
            //no rewrapping
            throw wse;
        } catch (RuntimeException re) {
            remedyActionTaken = true;

            throw new WebServiceException(re);

        }
        if (!handlerResult) {
            remedyActionTaken = true;
        }
        return handlerResult;
    }

    void callHandlersOnResponse(MessageUpdatableContext context, boolean handleFault) {
        try {

            //CLIENT-SIDE
            processor.callHandlersResponse(HandlerProcessor.Direction.INBOUND, context, handleFault);

        } catch (WebServiceException wse) {
            //no rewrapping
            throw wse;
        } catch (RuntimeException re) {
            throw new WebServiceException(re);
        }
    }

    void closeHandlers(MessageContext mc) {
        closeClientsideHandlers(mc);

    }
}
