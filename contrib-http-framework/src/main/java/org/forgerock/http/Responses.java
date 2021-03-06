/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2016 ForgeRock AS.
 */

package org.forgerock.http;

import static org.forgerock.util.promise.Promises.newResultPromise;

import org.forgerock.http.protocol.Response;
import org.forgerock.http.protocol.Status;
import org.forgerock.util.AsyncFunction;
import org.forgerock.util.Function;
import org.forgerock.util.promise.NeverThrowsException;
import org.forgerock.util.promise.Promise;

/**
 * Provide out-of-the-box, pre-configured {@link Response} objects.
 */
public final class Responses {
    private static final AsyncFunction<Exception, Response, NeverThrowsException> INTERNAL_SERVER_ERROR_ASYNC_FUNC =
            new AsyncFunction<Exception, Response, NeverThrowsException>() {
                @Override
                public Promise<Response, NeverThrowsException> apply(Exception e) {
                    return newResultPromise(newInternalServerError(e));
                }
            };

    /**
     * Generates an empty {@literal Internal Server Error} response ({@literal 500}).
     *
     * @return an empty {@literal Internal Server Error} response ({@literal 500}).
     */
    public static Response newInternalServerError() {
        return new Response(Status.INTERNAL_SERVER_ERROR);
    }

    private static final Function<NeverThrowsException, Object, Exception> NOOP_EXCEPTION_FUNC =
            new Function<NeverThrowsException, Object, Exception>() {
                @Override
                public Object apply(final NeverThrowsException value) throws Exception {
                    return null;
                }
            };

    /**
     * Generates an empty {@literal Not Found} response ({@literal 404}).
     *
     * @return an empty {@literal Not Found} response ({@literal 404}).
     */
    public static Response newNotFound() {
        return new Response(Status.NOT_FOUND);
    }

    /**
     * Utility method returning an empty function, whose goal is to ease the transformation of a
     * {@link org.forgerock.util.promise.Promise} type. Its main usage will be as the second argument in
     * {@link org.forgerock.util.promise.Promise#then(Function, Function)}. The implementation of this function is just
     * to return null : as its name suggests it, an {@code Exception} of type {@link NeverThrowsException} will never
     * be thrown.
     *
     * @param <V>
     *         The expected type of that function
     * @param <E>
     *         The new {@link Exception} that can be thrown by this function.
     * @return a function that will return {@literal null} and not throw any {@code Exception}.
     */
    @SuppressWarnings("unchecked")
    public static <V, E extends Exception> Function<NeverThrowsException, V, E> noopExceptionFunction() {
        return (Function<NeverThrowsException, V, E>) NOOP_EXCEPTION_FUNC;
    }

    /**
     * Utility function that returns a {@link Response} whose status is {@link Status#INTERNAL_SERVER_ERROR} and the
     * exception attached to the response as the cause.
     *
     * @param <E>
     *         The type of the incoming exception
     * @return {@link Response} whose status is {@link Status#INTERNAL_SERVER_ERROR} and the
     * exception attached to the response as the cause.
     */
    public static <E extends Exception> Function<E, Response, NeverThrowsException> onExceptionInternalServerError() {
        return new Function<E, Response, NeverThrowsException>() {
            @Override
            public Response apply(E e) {
                return newInternalServerError(e);
            }
        };
    }

    /**
     * Generates an {@literal Internal Server Error} response ({@literal 500})
     * containing the cause of the error response.
     *
     * @param exception
     *            wrapped exception
     * @return an empty {@literal Internal Server Error} response {@literal 500}
     *         with the cause set.
     */
    public static Response newInternalServerError(Exception exception) {
        return newInternalServerError().setCause(exception);
    }

    /**
     * Utility method returning an async function that creates a {@link Response} with status
     * {@link Status#INTERNAL_SERVER_ERROR} and the exception set as the cause.
     *
     * @param <E>
     *         The type of the incoming {@link Exception}
     * @return an async function that creates a {@link Response} with status {@link Status#INTERNAL_SERVER_ERROR}
     * and the exception set as the cause.
     */
    @SuppressWarnings("unchecked")
    public static <E extends Exception> AsyncFunction<E, Response, NeverThrowsException> internalServerError() {
        return (AsyncFunction<E, Response, NeverThrowsException>) INTERNAL_SERVER_ERROR_ASYNC_FUNC;
    }

    /**
     * Empty private constructor for utility.
     */
    private Responses() { }

}
