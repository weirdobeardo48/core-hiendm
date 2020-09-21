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

package com.hh.xml.internal.ws.encoding;

import com.sun.istack.internal.NotNull;
import com.hh.xml.internal.stream.buffer.MutableXMLStreamBuffer;
import com.hh.xml.internal.stream.buffer.XMLStreamBuffer;
import com.hh.xml.internal.stream.buffer.XMLStreamBufferMark;
import com.hh.xml.internal.stream.buffer.stax.StreamReaderBufferCreator;
import com.hh.xml.internal.ws.api.SOAPVersion;
import com.hh.xml.internal.ws.api.message.AttachmentSet;
import com.hh.xml.internal.ws.api.message.HeaderList;
import com.hh.xml.internal.ws.api.message.Message;
import com.hh.xml.internal.ws.api.message.Packet;
import com.hh.xml.internal.ws.api.pipe.ContentType;
import com.hh.xml.internal.ws.api.streaming.XMLStreamReaderFactory;
import com.hh.xml.internal.ws.api.streaming.XMLStreamWriterFactory;
import com.hh.xml.internal.ws.message.AttachmentSetImpl;
import com.hh.xml.internal.ws.message.stream.StreamHeader;
import com.hh.xml.internal.ws.message.stream.StreamMessage;
import com.hh.xml.internal.ws.protocol.soap.VersionMismatchException;
import com.hh.xml.internal.ws.server.UnsupportedMediaException;
import com.hh.xml.internal.ws.streaming.XMLStreamReaderUtil;
import com.hh.xml.internal.ws.streaming.TidyXMLStreamReader;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import com.hh.webservice.ws.WebServiceException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A stream SOAP codec.
 *
 * @author Paul Sandoz
 */
@SuppressWarnings({"StringEquality"})
public abstract class StreamSOAPCodec implements com.hh.xml.internal.ws.api.pipe.StreamSOAPCodec, RootOnlyCodec {

    private static final String SOAP_ENVELOPE = "Envelope";
    private static final String SOAP_HEADER = "Header";
    private static final String SOAP_BODY = "Body";

    private final String SOAP_NAMESPACE_URI;
    private final SOAPVersion soapVersion;

    /*package*/ StreamSOAPCodec(SOAPVersion soapVersion) {
        SOAP_NAMESPACE_URI = soapVersion.nsUri;
        this.soapVersion = soapVersion;
    }

    // consider caching
    // private final XMLStreamReader reader;

    // consider caching
    // private final MutableXMLStreamBuffer buffer;

    public ContentType getStaticContentType(Packet packet) {
        return getContentType(packet.soapAction);
    }

    public ContentType encode(Packet packet, OutputStream out) {
        if (packet.getMessage() != null) {
            XMLStreamWriter writer = XMLStreamWriterFactory.create(out);
            try {
                packet.getMessage().writeTo(writer);
                writer.flush();
            } catch (XMLStreamException e) {
                throw new WebServiceException(e);
            }
            XMLStreamWriterFactory.recycle(writer);
        }
        return getContentType(packet.soapAction);
    }

    protected abstract ContentType getContentType(String soapAction);

    public ContentType encode(Packet packet, WritableByteChannel buffer) {
        //TODO: not yet implemented
        throw new UnsupportedOperationException();
    }

    protected abstract List<String> getExpectedContentTypes();

    public void decode(InputStream in, String contentType, Packet packet) throws IOException {
        decode(in, contentType, packet, new AttachmentSetImpl());
    }

    /*
     * Checks against expected Content-Type headers that is handled by a codec
     *
     * @param ct the Content-Type of the request
     * @param expected expected Content-Types for a codec
     * @return true if the codec supports this Content-Type
     *         false otherwise
     */
    private static boolean isContentTypeSupported(String ct, List<String> expected) {
        if(ct.contains("text/xml")) return true;
        for(String contentType : expected) {
            if (ct.indexOf(contentType) != -1) {
                return true;
            }
        }
        return false;
    }

    /**
     * Decodes a message from {@link XMLStreamReader} that points to
     * the beginning of a SOAP infoset.
     *
     * @param reader
     *      can point to the start document or the start element.
     */
    public final @NotNull Message decode(@NotNull XMLStreamReader reader) {
        return decode(reader,new AttachmentSetImpl());
    }

    /**
     * Decodes a message from {@link XMLStreamReader} that points to
     * the beginning of a SOAP infoset.
     *
     * @param reader
     *      can point to the start document or the start element.
     * @param attachmentSet
     *      {@link StreamSOAPCodec} can take attachments parsed outside,
     *      so that this codec can be used as a part of a biggre codec
     *      (like MIME multipart codec.)
     */
    public final Message decode(XMLStreamReader reader, @NotNull AttachmentSet attachmentSet) {

        // Move to soap:Envelope and verify
        if(reader.getEventType()!=XMLStreamConstants.START_ELEMENT)
            XMLStreamReaderUtil.nextElementContent(reader);
        XMLStreamReaderUtil.verifyReaderState(reader,XMLStreamConstants.START_ELEMENT);
        if (SOAP_ENVELOPE.equals(reader.getLocalName()) && !SOAP_NAMESPACE_URI.equals(reader.getNamespaceURI())) {
            throw new VersionMismatchException(soapVersion, SOAP_NAMESPACE_URI, reader.getNamespaceURI());
        }
        XMLStreamReaderUtil.verifyTag(reader, SOAP_NAMESPACE_URI, SOAP_ENVELOPE);

        TagInfoset envelopeTag = new TagInfoset(reader);

        // Collect namespaces on soap:Envelope
        Map<String,String> namespaces = new HashMap<String,String>();
        for(int i=0; i< reader.getNamespaceCount();i++){
                namespaces.put(reader.getNamespacePrefix(i), reader.getNamespaceURI(i));
        }

        // Move to next element
        XMLStreamReaderUtil.nextElementContent(reader);
        XMLStreamReaderUtil.verifyReaderState(reader,
                javax.xml.stream.XMLStreamConstants.START_ELEMENT);

        HeaderList headers = null;
        TagInfoset headerTag = null;

        if (reader.getLocalName().equals(SOAP_HEADER)
                && reader.getNamespaceURI().equals(SOAP_NAMESPACE_URI)) {
            headerTag = new TagInfoset(reader);

            // Collect namespaces on soap:Header
            for(int i=0; i< reader.getNamespaceCount();i++){
                namespaces.put(reader.getNamespacePrefix(i), reader.getNamespaceURI(i));
            }
            // skip <soap:Header>
            XMLStreamReaderUtil.nextElementContent(reader);

            // If SOAP header blocks are present (i.e. not <soap:Header/>)
            if (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
                headers = new HeaderList();

                try {
                    // Cache SOAP header blocks
                    cacheHeaders(reader, namespaces, headers);
                } catch (XMLStreamException e) {
                    // TODO need to throw more meaningful exception
                    throw new WebServiceException(e);
                }
            }

            // Move to soap:Body
            XMLStreamReaderUtil.nextElementContent(reader);
        }

        // Verify that <soap:Body> is present
        XMLStreamReaderUtil.verifyTag(reader, SOAP_NAMESPACE_URI, SOAP_BODY);
        TagInfoset bodyTag = new TagInfoset(reader);

        XMLStreamReaderUtil.nextElementContent(reader);
        return new StreamMessage(envelopeTag,headerTag,attachmentSet,headers,bodyTag,reader,soapVersion);
        // when there's no payload,
        // it's tempting to use EmptyMessageImpl, but it doesn't presere the infoset
        // of <envelope>,<header>, and <body>, so we need to stick to StreamMessage.
    }

    public void decode(ReadableByteChannel in, String contentType, Packet packet ) {
        throw new UnsupportedOperationException();
    }

    public final StreamSOAPCodec copy() {
        return this;
    }

    private XMLStreamBuffer cacheHeaders(XMLStreamReader reader,
            Map<String, String> namespaces, HeaderList headers) throws XMLStreamException {
        MutableXMLStreamBuffer buffer = createXMLStreamBuffer();
        StreamReaderBufferCreator creator = new StreamReaderBufferCreator();
        creator.setXMLStreamBuffer(buffer);

        // Reader is positioned at the first header block
        while(reader.getEventType() == javax.xml.stream.XMLStreamConstants.START_ELEMENT) {
            Map<String,String> headerBlockNamespaces = namespaces;

            // Collect namespaces on SOAP header block
            if (reader.getNamespaceCount() > 0) {
                headerBlockNamespaces = new HashMap<String,String>(namespaces);
                for (int i = 0; i < reader.getNamespaceCount(); i++) {
                    headerBlockNamespaces.put(reader.getNamespacePrefix(i), reader.getNamespaceURI(i));
                }
            }

            // Mark
            XMLStreamBuffer mark = new XMLStreamBufferMark(headerBlockNamespaces, creator);
            // Create Header
            headers.add(createHeader(reader, mark));


            // Cache the header block
            // After caching Reader will be positioned at next header block or
            // the end of the </soap:header>
            creator.createElementFragment(reader, false);
            if (reader.getEventType() != XMLStreamConstants.START_ELEMENT &&
                    reader.getEventType() != XMLStreamConstants.END_ELEMENT) {
                XMLStreamReaderUtil.nextElementContent(reader);
            }
        }

        return buffer;
    }

    protected abstract StreamHeader createHeader(XMLStreamReader reader, XMLStreamBuffer mark);

    private MutableXMLStreamBuffer createXMLStreamBuffer() {
        // TODO: Decode should own one MutableXMLStreamBuffer for reuse
        // since it is more efficient. ISSUE: possible issue with
        // lifetime of information in the buffer if accessed beyond
        // the pipe line.
        return new MutableXMLStreamBuffer();
    }

    public void decode(InputStream in, String contentType, Packet packet, AttachmentSet att ) throws IOException {
        List<String> expectedContentTypes = getExpectedContentTypes();
        if (contentType != null && !isContentTypeSupported(contentType,expectedContentTypes)) {
            throw new UnsupportedMediaException(contentType, expectedContentTypes);
        }
        String charset = new ContentTypeImpl(contentType).getCharSet();
        if (charset != null && !Charset.isSupported(charset)) {
            throw new UnsupportedMediaException(charset);
        }
        XMLStreamReader reader = XMLStreamReaderFactory.create(null, in, charset, true);
        reader =  new TidyXMLStreamReader(reader, in);
        packet.setMessage(decode(reader, att));
    }

    public void decode(ReadableByteChannel in, String contentType, Packet response, AttachmentSet att ) {
        throw new UnsupportedOperationException();
    }



    /**
     * Creates a new {@link StreamSOAPCodec} instance.
     */
    public static StreamSOAPCodec create(SOAPVersion version) {
        if(version==null)
            // this decoder is for SOAP, not for XML/HTTP
            throw new IllegalArgumentException();
        switch(version) {
            case SOAP_11:
                return new StreamSOAP11Codec();
            case SOAP_12:
                return new StreamSOAP12Codec();
            default:
                throw new AssertionError();
        }
    }

}