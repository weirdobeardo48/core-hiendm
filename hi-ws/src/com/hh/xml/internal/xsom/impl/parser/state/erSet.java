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

/* this file is generated by RelaxNGCC */
package com.hh.xml.internal.xsom.impl.parser.state;
import com.hh.xml.internal.xsom.XSType;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import com.hh.xml.internal.xsom.impl.parser.NGCCRuntimeEx;



class erSet extends NGCCHandler {
    private String v;
    protected final NGCCRuntimeEx $runtime;
    private int $_ngcc_current_state;
    protected String $uri;
    protected String $localName;
    protected String $qname;

    public final NGCCRuntime getRuntime() {
        return($runtime);
    }

    public erSet(NGCCHandler parent, NGCCEventSource source, NGCCRuntimeEx runtime, int cookie) {
        super(source, parent, cookie);
        $runtime = runtime;
        $_ngcc_current_state = 1;
    }

    public erSet(NGCCRuntimeEx runtime) {
        this(null, runtime, runtime, -1);
    }

    public void enterElement(String $__uri, String $__local, String $__qname, Attributes $attrs) throws SAXException {
        int $ai;
        $uri = $__uri;
        $localName = $__local;
        $qname = $__qname;
        switch($_ngcc_current_state) {
        case 0:
            {
                revertToParentFromEnterElement(makeResult(), super._cookie, $__uri, $__local, $__qname, $attrs);
            }
            break;
        default:
            {
                unexpectedEnterElement($__qname);
            }
            break;
        }
    }

    public void leaveElement(String $__uri, String $__local, String $__qname) throws SAXException {
        int $ai;
        $uri = $__uri;
        $localName = $__local;
        $qname = $__qname;
        switch($_ngcc_current_state) {
        case 0:
            {
                revertToParentFromLeaveElement(makeResult(), super._cookie, $__uri, $__local, $__qname);
            }
            break;
        default:
            {
                unexpectedLeaveElement($__qname);
            }
            break;
        }
    }

    public void enterAttribute(String $__uri, String $__local, String $__qname) throws SAXException {
        int $ai;
        $uri = $__uri;
        $localName = $__local;
        $qname = $__qname;
        switch($_ngcc_current_state) {
        case 0:
            {
                revertToParentFromEnterAttribute(makeResult(), super._cookie, $__uri, $__local, $__qname);
            }
            break;
        default:
            {
                unexpectedEnterAttribute($__qname);
            }
            break;
        }
    }

    public void leaveAttribute(String $__uri, String $__local, String $__qname) throws SAXException {
        int $ai;
        $uri = $__uri;
        $localName = $__local;
        $qname = $__qname;
        switch($_ngcc_current_state) {
        case 0:
            {
                revertToParentFromLeaveAttribute(makeResult(), super._cookie, $__uri, $__local, $__qname);
            }
            break;
        default:
            {
                unexpectedLeaveAttribute($__qname);
            }
            break;
        }
    }

    public void text(String $value) throws SAXException {
        int $ai;
        switch($_ngcc_current_state) {
        case 1:
            {
                v = $value;
                $_ngcc_current_state = 0;
            }
            break;
        case 0:
            {
                revertToParentFromText(makeResult(), super._cookie, $value);
            }
            break;
        }
    }

    public void onChildCompleted(Object $__result__, int $__cookie__, boolean $__needAttCheck__)throws SAXException {
        switch($__cookie__) {
        }
    }

    public boolean accepted() {
        return(($_ngcc_current_state == 0));
    }


      private Integer makeResult() {
        if(v==null)     return new Integer($runtime.finalDefault);

        if(v.indexOf("#all")!=-1)
            return new Integer(XSType.EXTENSION|XSType.RESTRICTION);

        int r = 0;

        if(v.indexOf("extension")!=-1)      r|=XSType.EXTENSION;
        if(v.indexOf("restriction")!=-1)    r|=XSType.RESTRICTION;

        return new Integer(r);
      }

}
