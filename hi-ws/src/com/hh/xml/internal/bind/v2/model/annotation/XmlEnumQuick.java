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

package com.hh.xml.internal.bind.v2.model.annotation;

import java.lang.annotation.Annotation;
import javax.xml.bind.annotation.XmlEnum;

final class XmlEnumQuick
    extends Quick
    implements XmlEnum
{

    private final XmlEnum core;

    public XmlEnumQuick(Locatable upstream, XmlEnum core) {
        super(upstream);
        this.core = core;
    }

    protected Annotation getAnnotation() {
        return core;
    }

    protected Quick newInstance(Locatable upstream, Annotation core) {
        return new XmlEnumQuick(upstream, ((XmlEnum) core));
    }

    public Class<XmlEnum> annotationType() {
        return XmlEnum.class;
    }

    public Class value() {
        return core.value();
    }

}
