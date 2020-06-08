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
import com.hh.xml.internal.xsom.XSAttGroupDecl;
import com.hh.xml.internal.xsom.XSComplexType;
import com.hh.xml.internal.xsom.XSModelGroupDecl;
import com.hh.xml.internal.xsom.XSSimpleType;
import com.hh.xml.internal.xsom.impl.AttGroupDeclImpl;
import com.hh.xml.internal.xsom.impl.ComplexTypeImpl;
import com.hh.xml.internal.xsom.impl.ModelGroupDeclImpl;
import com.hh.xml.internal.xsom.impl.SimpleTypeImpl;
import com.hh.xml.internal.xsom.impl.parser.Messages;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import com.hh.xml.internal.xsom.impl.parser.NGCCRuntimeEx;
    import com.hh.xml.internal.xsom.parser.AnnotationContext;



class redefine extends NGCCHandler {
    private String schemaLocation;
    private ModelGroupDeclImpl newGrp;
    private AttGroupDeclImpl newAg;
    private SimpleTypeImpl newSt;
    private ComplexTypeImpl newCt;
    protected final NGCCRuntimeEx $runtime;
    private int $_ngcc_current_state;
    protected String $uri;
    protected String $localName;
    protected String $qname;

    public final NGCCRuntime getRuntime() {
        return($runtime);
    }

    public redefine(NGCCHandler parent, NGCCEventSource source, NGCCRuntimeEx runtime, int cookie) {
        super(source, parent, cookie);
        $runtime = runtime;
        $_ngcc_current_state = 15;
    }

    public redefine(NGCCRuntimeEx runtime) {
        this(null, runtime, runtime, -1);
    }

    private void action0()throws SAXException {
        XSAttGroupDecl oldAg = $runtime.currentSchema.getAttGroupDecl(newAg.getName());
            if(oldAg==null) {
              $runtime.reportError( Messages.format(Messages.ERR_UNDEFINED_ATTRIBUTEGROUP,newAg.getName()) );
            } else {
              newAg.redefine((AttGroupDeclImpl)oldAg);
              $runtime.currentSchema.addAttGroupDecl(newAg,true);
            }
}

    private void action1()throws SAXException {
        XSModelGroupDecl oldGrp = $runtime.currentSchema.getModelGroupDecl(newGrp.getName());
            if(oldGrp==null) {
              $runtime.reportError( Messages.format(Messages.ERR_UNDEFINED_MODELGROUP,newGrp.getName()) );
            } else {
              newGrp.redefine((ModelGroupDeclImpl)oldGrp);
              $runtime.currentSchema.addModelGroupDecl(newGrp,true);
            }
}

    private void action2()throws SAXException {
        XSComplexType oldCt = $runtime.currentSchema.getComplexType(newCt.getName());
            if(oldCt==null) {
              $runtime.reportError( Messages.format(Messages.ERR_UNDEFINED_COMPLEXTYPE,newCt.getName()) );
            } else {
              newCt.redefine((ComplexTypeImpl)oldCt);
              $runtime.currentSchema.addComplexType(newCt,true);
            }
}

    private void action3()throws SAXException {
        XSSimpleType oldSt = $runtime.currentSchema.getSimpleType(newSt.getName());
            if(oldSt==null) {
              $runtime.reportError( Messages.format(Messages.ERR_UNDEFINED_SIMPLETYPE,newSt.getName()) );
            } else {
              newSt.redefine((SimpleTypeImpl)oldSt);
              $runtime.currentSchema.addSimpleType(newSt,true);
            }
}

    private void action4()throws SAXException {
        $runtime.includeSchema( schemaLocation );
}

    public void enterElement(String $__uri, String $__local, String $__qname, Attributes $attrs) throws SAXException {
        int $ai;
        $uri = $__uri;
        $localName = $__local;
        $qname = $__qname;
        switch($_ngcc_current_state) {
        case 2:
            {
                if(($__uri.equals("http://www.w3.org/2001/XMLSchema") && $__local.equals("annotation"))) {
                    NGCCHandler h = new annotation(this, super._source, $runtime, 581, null,AnnotationContext.SCHEMA);
                    spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
                }
                else {
                    if(($__uri.equals("http://www.w3.org/2001/XMLSchema") && $__local.equals("simpleType"))) {
                        NGCCHandler h = new simpleType(this, super._source, $runtime, 582);
                        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
                    }
                    else {
                        if(($__uri.equals("http://www.w3.org/2001/XMLSchema") && $__local.equals("complexType"))) {
                            NGCCHandler h = new complexType(this, super._source, $runtime, 583);
                            spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
                        }
                        else {
                            if(($__uri.equals("http://www.w3.org/2001/XMLSchema") && $__local.equals("group"))) {
                                NGCCHandler h = new group(this, super._source, $runtime, 584);
                                spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
                            }
                            else {
                                if(($__uri.equals("http://www.w3.org/2001/XMLSchema") && $__local.equals("attributeGroup"))) {
                                    NGCCHandler h = new attributeGroupDecl(this, super._source, $runtime, 585);
                                    spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
                                }
                                else {
                                    $_ngcc_current_state = 1;
                                    $runtime.sendEnterElement(super._cookie, $__uri, $__local, $__qname, $attrs);
                                }
                            }
                        }
                    }
                }
            }
            break;
        case 15:
            {
                if(($__uri.equals("http://www.w3.org/2001/XMLSchema") && $__local.equals("redefine"))) {
                    $runtime.onEnterElementConsumed($__uri, $__local, $__qname, $attrs);
                    $_ngcc_current_state = 14;
                }
                else {
                    unexpectedEnterElement($__qname);
                }
            }
            break;
        case 0:
            {
                revertToParentFromEnterElement(this, super._cookie, $__uri, $__local, $__qname, $attrs);
            }
            break;
        case 14:
            {
                if(($ai = $runtime.getAttributeIndex("","schemaLocation"))>=0) {
                    $runtime.consumeAttribute($ai);
                    $runtime.sendEnterElement(super._cookie, $__uri, $__local, $__qname, $attrs);
                }
                else {
                    unexpectedEnterElement($__qname);
                }
            }
            break;
        case 1:
            {
                if(($__uri.equals("http://www.w3.org/2001/XMLSchema") && $__local.equals("annotation"))) {
                    NGCCHandler h = new annotation(this, super._source, $runtime, 576, null,AnnotationContext.SCHEMA);
                    spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
                }
                else {
                    if(($__uri.equals("http://www.w3.org/2001/XMLSchema") && $__local.equals("simpleType"))) {
                        NGCCHandler h = new simpleType(this, super._source, $runtime, 577);
                        spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
                    }
                    else {
                        if(($__uri.equals("http://www.w3.org/2001/XMLSchema") && $__local.equals("complexType"))) {
                            NGCCHandler h = new complexType(this, super._source, $runtime, 578);
                            spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
                        }
                        else {
                            if(($__uri.equals("http://www.w3.org/2001/XMLSchema") && $__local.equals("group"))) {
                                NGCCHandler h = new group(this, super._source, $runtime, 579);
                                spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
                            }
                            else {
                                if(($__uri.equals("http://www.w3.org/2001/XMLSchema") && $__local.equals("attributeGroup"))) {
                                    NGCCHandler h = new attributeGroupDecl(this, super._source, $runtime, 580);
                                    spawnChildFromEnterElement(h, $__uri, $__local, $__qname, $attrs);
                                }
                                else {
                                    unexpectedEnterElement($__qname);
                                }
                            }
                        }
                    }
                }
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
        case 2:
            {
                $_ngcc_current_state = 1;
                $runtime.sendLeaveElement(super._cookie, $__uri, $__local, $__qname);
            }
            break;
        case 0:
            {
                revertToParentFromLeaveElement(this, super._cookie, $__uri, $__local, $__qname);
            }
            break;
        case 14:
            {
                if(($ai = $runtime.getAttributeIndex("","schemaLocation"))>=0) {
                    $runtime.consumeAttribute($ai);
                    $runtime.sendLeaveElement(super._cookie, $__uri, $__local, $__qname);
                }
                else {
                    unexpectedLeaveElement($__qname);
                }
            }
            break;
        case 1:
            {
                if(($__uri.equals("http://www.w3.org/2001/XMLSchema") && $__local.equals("redefine"))) {
                    $runtime.onLeaveElementConsumed($__uri, $__local, $__qname);
                    $_ngcc_current_state = 0;
                }
                else {
                    unexpectedLeaveElement($__qname);
                }
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
        case 2:
            {
                $_ngcc_current_state = 1;
                $runtime.sendEnterAttribute(super._cookie, $__uri, $__local, $__qname);
            }
            break;
        case 0:
            {
                revertToParentFromEnterAttribute(this, super._cookie, $__uri, $__local, $__qname);
            }
            break;
        case 14:
            {
                if(($__uri.equals("") && $__local.equals("schemaLocation"))) {
                    $_ngcc_current_state = 13;
                }
                else {
                    unexpectedEnterAttribute($__qname);
                }
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
        case 2:
            {
                $_ngcc_current_state = 1;
                $runtime.sendLeaveAttribute(super._cookie, $__uri, $__local, $__qname);
            }
            break;
        case 12:
            {
                if(($__uri.equals("") && $__local.equals("schemaLocation"))) {
                    $_ngcc_current_state = 2;
                }
                else {
                    unexpectedLeaveAttribute($__qname);
                }
            }
            break;
        case 0:
            {
                revertToParentFromLeaveAttribute(this, super._cookie, $__uri, $__local, $__qname);
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
        case 2:
            {
                $_ngcc_current_state = 1;
                $runtime.sendText(super._cookie, $value);
            }
            break;
        case 13:
            {
                schemaLocation = $value;
                $_ngcc_current_state = 12;
                action4();
            }
            break;
        case 0:
            {
                revertToParentFromText(this, super._cookie, $value);
            }
            break;
        case 14:
            {
                if(($ai = $runtime.getAttributeIndex("","schemaLocation"))>=0) {
                    $runtime.consumeAttribute($ai);
                    $runtime.sendText(super._cookie, $value);
                }
            }
            break;
        }
    }

    public void onChildCompleted(Object $__result__, int $__cookie__, boolean $__needAttCheck__)throws SAXException {
        switch($__cookie__) {
        case 581:
            {
                $_ngcc_current_state = 1;
            }
            break;
        case 582:
            {
                newSt = ((SimpleTypeImpl)$__result__);
                action3();
                $_ngcc_current_state = 1;
            }
            break;
        case 583:
            {
                newCt = ((ComplexTypeImpl)$__result__);
                action2();
                $_ngcc_current_state = 1;
            }
            break;
        case 584:
            {
                newGrp = ((ModelGroupDeclImpl)$__result__);
                action1();
                $_ngcc_current_state = 1;
            }
            break;
        case 585:
            {
                newAg = ((AttGroupDeclImpl)$__result__);
                action0();
                $_ngcc_current_state = 1;
            }
            break;
        case 576:
            {
                $_ngcc_current_state = 1;
            }
            break;
        case 577:
            {
                newSt = ((SimpleTypeImpl)$__result__);
                action3();
                $_ngcc_current_state = 1;
            }
            break;
        case 578:
            {
                newCt = ((ComplexTypeImpl)$__result__);
                action2();
                $_ngcc_current_state = 1;
            }
            break;
        case 579:
            {
                newGrp = ((ModelGroupDeclImpl)$__result__);
                action1();
                $_ngcc_current_state = 1;
            }
            break;
        case 580:
            {
                newAg = ((AttGroupDeclImpl)$__result__);
                action0();
                $_ngcc_current_state = 1;
            }
            break;
        }
    }

    public boolean accepted() {
        return(($_ngcc_current_state == 0));
    }


}
