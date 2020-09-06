/*
 * Copyright (c) 2006, Oracle and/or its affiliates. All rights reserved.
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

package com.hh.net.httpserver;

/**
 * Authenticator represents an implementation of an HTTP authentication
 * mechanism. Sub-classes provide implementations of specific mechanisms
 * such as Digest or Basic auth. Instances are invoked to provide verification
 * of the authentication information provided in all incoming requests.
 * Note. This implies that any caching of credentials or other authentication
 * information must be done outside of this class.
 */
public abstract class Authenticator {

    /**
     * called to authenticate each incoming request. The implementation
     * must return a Failure, Success or Retry object as appropriate :-
     * <p>
     * Failure means the authentication has completed, but has failed
     * due to invalid credentials.
     * <p>
     * Sucess means that the authentication
     * has succeeded, and a Principal object representing the user
     * can be retrieved by calling Sucess.getPrincipal() .
     * <p>
     * Retry means that another HTTP exchange is required. Any response
     * headers needing to be sent back to the client are set in the
     * given HttpExchange. The response code to be returned must be provided
     * in the Retry object. Retry may occur multiple times.
     */
    public abstract Result authenticate(HttpExchange exch);

    /**
     * Base class for return type from authenticate() method
     */
    public abstract static class Result {
    }

    /**
     * Indicates an authentication failure. The authentication
     * attempt has completed.
     */
    public static class Failure extends Result {

        private int responseCode;

        public Failure(int responseCode) {
            this.responseCode = responseCode;
        }

        /**
         * returns the response code to send to the client
         */
        public int getResponseCode() {
            return responseCode;
        }
    }

    /**
     * Indicates an authentication has succeeded and the
     * authenticated user principal can be acquired by calling
     * getPrincipal().
     */
    public static class Success extends Result {
        private HttpPrincipal principal;

        public Success(HttpPrincipal p) {
            principal = p;
        }

        /**
         * returns the authenticated user Principal
         */
        public HttpPrincipal getPrincipal() {
            return principal;
        }
    }

    /**
     * Indicates an authentication must be retried. The
     * response code to be sent back is as returned from
     * getResponseCode(). The Authenticator must also have
     * set any necessary response headers in the given HttpExchange
     * before returning this Retry object.
     */
    public static class Retry extends Result {

        private int responseCode;

        public Retry(int responseCode) {
            this.responseCode = responseCode;
        }

        /**
         * returns the response code to send to the client
         */
        public int getResponseCode() {
            return responseCode;
        }
    }
}
