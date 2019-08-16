/**
 * GatewayRefundQueryServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.ewfresh.pay.util.bill99.bill99Soap;

public class GatewayRefundQueryServiceLocator extends org.apache.axis.client.Service implements GatewayRefundQueryService {

    public GatewayRefundQueryServiceLocator(String gatewayRefundQuery_address) {
        this.gatewayRefundQuery_address = gatewayRefundQuery_address;
    }


    public GatewayRefundQueryServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public GatewayRefundQueryServiceLocator(String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for gatewayRefundQuery
    // comment by jiudongdong
//    private String gatewayRefundQuery_address = "https://sandbox.99bill.com/gatewayapi/services/gatewayRefundQuery";
    private String gatewayRefundQuery_address;

    public String getgatewayRefundQueryAddress() {
        return gatewayRefundQuery_address;
    }

    // The WSDD service name defaults to the port name.
    private String gatewayRefundQueryWSDDServiceName = "gatewayRefundQuery";

    public String getgatewayRefundQueryWSDDServiceName() {
        return gatewayRefundQueryWSDDServiceName;
    }

    public void setgatewayRefundQueryWSDDServiceName(String name) {
        gatewayRefundQueryWSDDServiceName = name;
    }

    public GatewayRefundQuery getgatewayRefundQuery() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(gatewayRefundQuery_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getgatewayRefundQuery(endpoint);
    }

    public GatewayRefundQuery getgatewayRefundQuery(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            GatewayRefundQuerySoapBindingStub _stub = new GatewayRefundQuerySoapBindingStub(portAddress, this);
            _stub.setPortName(getgatewayRefundQueryWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setgatewayRefundQueryEndpointAddress(String address) {
        gatewayRefundQuery_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (GatewayRefundQuery.class.isAssignableFrom(serviceEndpointInterface)) {
                GatewayRefundQuerySoapBindingStub _stub = new GatewayRefundQuerySoapBindingStub(new java.net.URL(gatewayRefundQuery_address), this);
                _stub.setPortName(getgatewayRefundQueryWSDDServiceName());
                return _stub;
            }
        }
        catch (Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        String inputPortName = portName.getLocalPart();
        if ("gatewayRefundQuery".equals(inputPortName)) {
            return getgatewayRefundQuery();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
//        return new javax.xml.namespace.QName("https://sandbox.99bill.com/gatewayapi/services/gatewayRefundQuery", "GatewayRefundQueryService");
        return new javax.xml.namespace.QName(gatewayRefundQuery_address, "GatewayRefundQueryService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
//            ports.add(new javax.xml.namespace.QName("https://sandbox.99bill.com/gatewayapi/services/gatewayRefundQuery", "gatewayRefundQuery"));
            ports.add(new javax.xml.namespace.QName(gatewayRefundQuery_address, "gatewayRefundQuery"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(String portName, String address) throws javax.xml.rpc.ServiceException {
        if ("gatewayRefundQuery".equals(portName)) {
            setgatewayRefundQueryEndpointAddress(address);
        } else { // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
