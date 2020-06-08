/*
 * Copyright (c) 2005, Oracle and/or its affiliates. All rights reserved.
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

package com.hh.xml.internal.stream.events ;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.Attribute;
import javax.xml.namespace.NamespaceContext;
import java.io.Writer;
import com.hh.xml.internal.stream.util.ReadOnlyIterator;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.events.Namespace;

/** Implementation of StartElementEvent.
 *
 * @author Neeraj Bajaj Sun Microsystems,Inc.
 * @author K.Venugopal Sun Microsystems,Inc.
 */

public class StartElementEvent extends DummyEvent
implements StartElement {

    private Map fAttributes;
    private List fNamespaces;
    private NamespaceContext fNamespaceContext = null;
    private QName fQName;

    public StartElementEvent(String prefix, String uri, String localpart) {
        this(new QName(uri, localpart, prefix));
    }

    public StartElementEvent(QName qname) {
        fQName = qname;
        init();
    }

    public StartElementEvent(StartElement startelement) {
        this(startelement.getName());
        addAttributes(startelement.getAttributes());
        addNamespaceAttributes(startelement.getNamespaces());
    }

    protected void init() {
        setEventType(XMLStreamConstants.START_ELEMENT);
        fAttributes = new HashMap();
        fNamespaces = new ArrayList();
    }

    public QName getName() {
        return fQName;
    }

    public void setName(QName qname) {
        this.fQName = qname;
    }

    public Iterator getAttributes() {
        if(fAttributes != null){
            Collection coll = fAttributes.values();
            return new ReadOnlyIterator(coll.iterator());
        }
        return new ReadOnlyIterator();
    }

    public Iterator getNamespaces() {
        if(fNamespaces != null){
            return new ReadOnlyIterator(fNamespaces.iterator());
        }
        return new ReadOnlyIterator();
    }

    public Attribute getAttributeByName(QName qname) {
        if(qname == null)
            return null;
        return (Attribute)fAttributes.get(qname);
    }

    public String getNamespace(){
        return fQName.getNamespaceURI();
    }

    public String getNamespaceURI(String prefix) {
        //check that URI was supplied when creating this startElement event and prefix matches
        if( getNamespace() != null && fQName.getPrefix().equals(prefix)) return getNamespace();
        //else check the namespace context
        if(fNamespaceContext != null)
            return fNamespaceContext.getNamespaceURI(prefix);
        return null;
    }

    /**
     * <p>Return a <code>String</code> representation of this
     * <code>StartElement</code> formatted as XML.</p>
     *
     * @return <code>String</code> representation of this
     *   <code>StartElement</code> formatted as XML.
     */
    public String toString() {

        StringBuffer startElement = new StringBuffer();

        // open element
        startElement.append("<");
        startElement.append(nameAsString());

        // add any attributes
        if (fAttributes != null) {
            Iterator it = this.getAttributes();
            Attribute attr = null;
            while (it.hasNext()) {
                attr = (Attribute) it.next();
                startElement.append(" ");
                startElement.append(attr.toString());
            }
        }

        // add any namespaces
        if (fNamespaces != null) {
            Iterator it = fNamespaces.iterator();
            Namespace attr = null;
            while (it.hasNext()) {
                attr = (Namespace) it.next();
                startElement.append(" ");
                startElement.append(attr.toString());
            }
        }

        // close start tag
        startElement.append(">");

        // return StartElement as a String
        return startElement.toString();
    }

    /** Return this event as String
     * @return String Event returned as string.
     */
    public String nameAsString() {
        if("".equals(fQName.getNamespaceURI()))
            return fQName.getLocalPart();
        if(fQName.getPrefix() != null)
            return "['" + fQName.getNamespaceURI() + "']:" + fQName.getPrefix() + ":" + fQName.getLocalPart();
        else
            return "['" + fQName.getNamespaceURI() + "']:" + fQName.getLocalPart();
    }


    /** Gets a read-only namespace context. If no context is
     * available this method will return an empty namespace context.
     * The NamespaceContext contains information about all namespaces
     * in scope for this StartElement.
     *
     * @return the current namespace context
     */
    public NamespaceContext getNamespaceContext() {
        return fNamespaceContext;
    }

    public void setNamespaceContext(NamespaceContext nc) {
        fNamespaceContext = nc;
    }

    protected void writeAsEncodedUnicodeEx(java.io.Writer writer)
    throws java.io.IOException
    {
        writer.write(toString());
    }

    void addAttribute(Attribute attr){
        if(attr.isNamespace()){
            fNamespaces.add(attr);
        }else{
            fAttributes.put(attr.getName(),attr);
        }
    }

    public void addAttributes(Iterator attrs){
        if(attrs == null)
            return;
        while(attrs.hasNext()){
            Attribute attr = (Attribute)attrs.next();
            fAttributes.put(attr.getName(),attr);
        }
    }

    void addNamespaceAttribute(Namespace attr){
        if(attr == null)
            return;
        fNamespaces.add(attr);
    }

    void addNamespaceAttributes(Iterator attrs){
        if(attrs == null)
            return;
        while(attrs.hasNext()){
            Namespace attr = (Namespace)attrs.next();
            fNamespaces.add(attr);
        }
    }

}
