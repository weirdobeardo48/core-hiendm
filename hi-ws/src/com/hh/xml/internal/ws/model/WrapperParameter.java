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

package com.hh.xml.internal.ws.model;

import com.hh.xml.internal.bind.api.CompositeStructure;
import com.hh.xml.internal.bind.api.TypeReference;
import com.hh.xml.internal.ws.api.model.JavaMethod;
import com.hh.xml.internal.ws.api.model.ParameterBinding;

import javax.jws.WebParam.Mode;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link ParameterImpl} that represents a wrapper,
 * which is a parameter that consists of multiple nested {@link ParameterImpl}s
 * within, which together form a body part.
 *
 * <p>
 * Java method parameters represented by nested {@link ParameterImpl}s will be
 * packed into a "wrapper bean" and it becomes the {@link ParameterImpl} for the
 * body.
 *
 * <p>
 * This parameter is only used for the {@link ParameterBinding#BODY} binding.
 * Other parameters that bind to other parts (such as headers or unbound)
 * will show up directly under {@link JavaMethod}.
 *
 * @author Vivek Pandey
 */
public class WrapperParameter extends ParameterImpl {
    protected final List<ParameterImpl> wrapperChildren = new ArrayList<ParameterImpl>();

    // TODO: wrapper parameter doesn't use 'typeRef' --- it only uses tag name.
    public WrapperParameter(JavaMethodImpl parent, TypeReference typeRef, Mode mode, int index) {
        super(parent, typeRef, mode, index);
    }

    /**
     *
     * @deprecated
     *      Why are you calling a method that always return true?
     */
    @Override
    public boolean isWrapperStyle() {
        return true;
    }

    /**
     * @return Returns the wrapperChildren.
     */
    public List<ParameterImpl> getWrapperChildren() {
        return wrapperChildren;
    }

    /**
     * Adds a new child parameter.
     *
     * @param wrapperChild
     */
    public void addWrapperChild(ParameterImpl wrapperChild) {
        wrapperChildren.add(wrapperChild);
        // must bind to body. see class javadoc
        assert wrapperChild.getBinding()== ParameterBinding.BODY;
    }

    public void clear(){
        wrapperChildren.clear();
    }

    @Override
    void fillTypes(List<TypeReference> types) {
        super.fillTypes(types);
        if(getParent().getBinding().isRpcLit()) {
            // for rpc/lit, we need to individually marshal/unmarshal wrapped values,
            // so their TypeReference needs to be collected
            assert getTypeReference().type==CompositeStructure.class;
            for (ParameterImpl p : wrapperChildren)
                p.fillTypes(types);
        }
    }
}
