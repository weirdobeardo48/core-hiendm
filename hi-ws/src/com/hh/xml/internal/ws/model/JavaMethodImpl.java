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

import com.sun.istack.internal.NotNull;
import com.hh.xml.internal.bind.api.TypeReference;
import com.hh.xml.internal.ws.api.model.JavaMethod;
import com.hh.xml.internal.ws.api.model.MEP;
import com.hh.xml.internal.ws.api.model.SEIModel;
import com.hh.xml.internal.ws.api.model.wsdl.WSDLBoundOperation;
import com.hh.xml.internal.ws.model.soap.SOAPBindingImpl;
import com.hh.xml.internal.ws.model.wsdl.WSDLBoundOperationImpl;
import com.hh.xml.internal.ws.model.wsdl.WSDLPortImpl;
import com.hh.xml.internal.ws.api.model.wsdl.WSDLFault;
import com.sun.istack.internal.Nullable;

import javax.xml.namespace.QName;
import com.hh.webservice.ws.Action;
import com.hh.webservice.ws.WebServiceException;
import javax.jws.WebMethod;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * Build this runtime model using java SEI and annotations
 *
 * @author Vivek Pandey
 */
public final class JavaMethodImpl implements JavaMethod {

    private String inputAction = "";
    private String outputAction = "";
    private final List<CheckedExceptionImpl> exceptions = new ArrayList<CheckedExceptionImpl>();
    private final Method method;
    /*package*/ final List<ParameterImpl> requestParams = new ArrayList<ParameterImpl>();
    /*package*/ final List<ParameterImpl> responseParams = new ArrayList<ParameterImpl>();
    private final List<ParameterImpl> unmReqParams = Collections.unmodifiableList(requestParams);
    private final List<ParameterImpl> unmResParams = Collections.unmodifiableList(responseParams);
    private SOAPBindingImpl binding;
    private MEP mep;
    private String operationName;
    private WSDLBoundOperationImpl wsdlOperation;
    /*package*/ final AbstractSEIModelImpl owner;
    private final Method seiMethod;

    /**
     * @param owner
     * @param method : Implementation class method
     * @param seiMethod : corresponding SEI Method.
     *                  Is there is no SEI, it should be Implementation class method
     */
    public JavaMethodImpl(AbstractSEIModelImpl owner, Method method, Method seiMethod) {
        this.owner = owner;
        this.method = method;
        this.seiMethod = seiMethod;
        setWsaActions();
    }

    private void setWsaActions() {
        Action action = seiMethod.getAnnotation(Action.class);
        if(action != null) {
            inputAction = action.input();
            outputAction = action.output();
        }

        //@Action(input) =="", get it from @WebMethod(action)
        WebMethod webMethod = seiMethod.getAnnotation(WebMethod.class);
        String soapAction = "";
        if (webMethod != null )
            soapAction = webMethod.action();
        if(!soapAction.equals("")) {
            //non-empty soapAction
            if(inputAction.equals(""))
                // set input action to non-empty soapAction
                inputAction = soapAction;
            else if(!inputAction.equals(soapAction)){
                //both are explicitly set via annotations, make sure @Action == @WebMethod.action
                throw new WebServiceException("@Action and @WebMethod(action=\"\" does not match on operation "+ method.getName());
            }
        }
    }

    public SEIModel getOwner() {
        return owner;
    }

    /**
     * @see {@link JavaMethod}
     *
     * @return Returns the method.
     */
    public Method getMethod() {
        return method;
    }

    /**
     * @see {@link JavaMethod}
     *
     * @return Returns the SEI method where annotations are present
     */
    public Method getSEIMethod() {
        return seiMethod;
    }

    /**
     * @return Returns the mep.
     */
    public MEP getMEP() {
        return mep;
    }

    /**
     * @param mep
     *            The mep to set.
     */
    void setMEP(MEP mep) {
        this.mep = mep;
    }

    /**
     * @return the Binding object
     */
    public SOAPBindingImpl getBinding() {
        if (binding == null)
            return new SOAPBindingImpl();
        return binding;
    }

    /**
     * @param binding
     */
    void setBinding(SOAPBindingImpl binding) {
        this.binding = binding;
    }

    /**
     * Returns the {@link WSDLBoundOperation} Operation associated with {@link this}
     * operation.
     *
     * @return the WSDLBoundOperation for this JavaMethod
     */
    public @NotNull WSDLBoundOperation getOperation() {
        assert wsdlOperation != null;
        return wsdlOperation;
    }

    public void setOperationName(String name) {
        this.operationName = name;
    }

    public String getOperationName() {
        return operationName;
    }

    public String getRequestMessageName() {
        return operationName;
    }

    public String getResponseMessageName() {
        if(mep.isOneWay())
            return null;
        return operationName+"Response";
    }

    /**
     * @return soap:Body's first child name for request message.
     */
    public @Nullable QName getRequestPayloadName() {
        return wsdlOperation.getReqPayloadName();
    }

    /**
     * @return soap:Body's first child name for response message.
     */
    public @Nullable QName getResponsePayloadName() {
        return (mep == MEP.ONE_WAY) ? null : wsdlOperation.getResPayloadName();
    }

    /**
     * @return returns unmodifiable list of request parameters
     */
    public List<ParameterImpl> getRequestParameters() {
        return unmReqParams;
    }

    /**
     * @return returns unmodifiable list of response parameters
     */
    public List<ParameterImpl> getResponseParameters() {
        return unmResParams;
    }

    void addParameter(ParameterImpl p) {
        if (p.isIN() || p.isINOUT()) {
            assert !requestParams.contains(p);
            requestParams.add(p);
        }

        if (p.isOUT() || p.isINOUT()) {
            // this check is only for out parameters
            assert !responseParams.contains(p);
            responseParams.add(p);
        }
    }

    void addRequestParameter(ParameterImpl p){
        if (p.isIN() || p.isINOUT()) {
            requestParams.add(p);
        }
    }

    void addResponseParameter(ParameterImpl p){
        if (p.isOUT() || p.isINOUT()) {
            responseParams.add(p);
        }
    }

    /**
     * @return Returns number of java method parameters - that will be all the
     *         IN, INOUT and OUT holders
     *
     * @deprecated no longer use in the new architecture
     */
    public int getInputParametersCount() {
        int count = 0;
        for (ParameterImpl param : requestParams) {
            if (param.isWrapperStyle()) {
                count += ((WrapperParameter) param).getWrapperChildren().size();
            } else {
                count++;
            }
        }

        for (ParameterImpl param : responseParams) {
            if (param.isWrapperStyle()) {
                for (ParameterImpl wc : ((WrapperParameter) param).getWrapperChildren()) {
                    if (!wc.isResponse() && wc.isOUT()) {
                        count++;
                    }
                }
            } else if (!param.isResponse() && param.isOUT()) {
                count++;
            }
        }

        return count;
    }

    /**
     * @param ce
     */
    void addException(CheckedExceptionImpl ce) {
        if (!exceptions.contains(ce))
            exceptions.add(ce);
    }

    /**
     * @param exceptionClass
     * @return CheckedException corresponding to the exceptionClass. Returns
     *         null if not found.
     */
    public CheckedExceptionImpl getCheckedException(Class exceptionClass) {
        for (CheckedExceptionImpl ce : exceptions) {
            if (ce.getExceptionClass()==exceptionClass)
                return ce;
        }
        return null;
    }


    /**
     * @return a list of checked Exceptions thrown by this method
     */
    public List<CheckedExceptionImpl> getCheckedExceptions(){
        return Collections.unmodifiableList(exceptions);
    }

    public String getInputAction() {
        return inputAction;
    }

    public String getOutputAction() {
        return outputAction;
    }

    /**
     * @param detailType
     * @return Gets the CheckedException corresponding to detailType. Returns
     *         null if no CheckedExcpetion with the detailType found.
     */
    public CheckedExceptionImpl getCheckedException(TypeReference detailType) {
        for (CheckedExceptionImpl ce : exceptions) {
            TypeReference actual = ce.getDetailType();
            if (actual.tagName.equals(detailType.tagName) && actual.type==detailType.type) {
                return ce;
            }
        }
        return null;
    }



    /**
     * Returns if the java method  is async
     * @return if this is an Asynch
     */
    public boolean isAsync(){
        return mep.isAsync;
    }

    /*package*/ void freeze(WSDLPortImpl portType) {
        this.wsdlOperation = portType.getBinding().get(new QName(portType.getBinding().getPortType().getName().getNamespaceURI(),operationName));
        // TODO: replace this with proper error handling
        if(wsdlOperation ==null)
            throw new WebServiceException("Method "+seiMethod.getName()+" is exposed as WebMethod, but there is no corresponding wsdl operation with name "+operationName+" in the wsdl:portType" + portType.getBinding().getPortType().getName());

        //so far, the inputAction, outputAction and fault actions are set from the @Action and @FaultAction
        //set the values from WSDLModel, if such annotations are not present or defaulted
        if(inputAction.equals("")) {
                inputAction = wsdlOperation.getOperation().getInput().getAction();
        } else if(!inputAction.equals(wsdlOperation.getOperation().getInput().getAction()))
                //TODO input action might be from @Action or WebMethod(action)
                LOGGER.warning("Input Action on WSDL operation "+wsdlOperation.getName().getLocalPart() + " and @Action on its associated Web Method " + seiMethod.getName() +" did not match and will cause problems in dispatching the requests");

        if (!mep.isOneWay()) {
            if (outputAction.equals(""))
                outputAction = wsdlOperation.getOperation().getOutput().getAction();

            for (CheckedExceptionImpl ce : exceptions) {
                if (ce.getFaultAction().equals("")) {
                    QName detailQName = ce.getDetailType().tagName;
                    WSDLFault wsdlfault = wsdlOperation.getOperation().getFault(detailQName);
                    if(wsdlfault == null) {
                        // mismatch between wsdl model and SEI model, log a warning and use  SEI model for Action determination
                        LOGGER.warning("Mismatch between Java model and WSDL model found, For wsdl operation " +
                                wsdlOperation.getName() + ",There is no matching wsdl fault with detail QName " +
                                ce.getDetailType().tagName);
                        ce.setFaultAction(ce.getDefaultFaultAction());
                    } else {
                        ce.setFaultAction(wsdlfault.getAction());
                    }
                }
            }
        }
    }

    final void fillTypes(List<TypeReference> types) {
        fillTypes(requestParams, types);
        fillTypes(responseParams, types);

        for (CheckedExceptionImpl ce : exceptions) {
            types.add(ce.getDetailType());
        }
    }

    private void fillTypes(List<ParameterImpl> params, List<TypeReference> types) {
        for (ParameterImpl p : params) {
            p.fillTypes(types);
        }
    }

    private static final Logger LOGGER = Logger.getLogger(com.hh.xml.internal.ws.model.JavaMethodImpl.class.getName());

}