/*
 * Copyright 2009 Kjetil Valstadsve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sf.vanadis.modules.httpprovider;

import com.sun.grizzly.tcp.http11.GrizzlyAdapter;
import com.sun.grizzly.tcp.http11.GrizzlyRequest;
import com.sun.grizzly.tcp.http11.GrizzlyResponse;
import net.sf.vanadis.core.io.Closeables;
import net.sf.vanadis.core.lang.ContextClassLoaderSwitch;
import net.sf.vanadis.core.reflection.ContextClassLoaderObjectInputStream;
import net.sf.vanadis.remoting.*;
import net.sf.vanadis.services.remoting.RemotingException;
import net.sf.vanadis.util.log.Log;
import net.sf.vanadis.util.log.Logs;

import java.io.*;

@SuppressWarnings({"RawUseOfParameterizedType"})
public class InvocationAdapter extends GrizzlyAdapter {

    private static final Log log = Logs.get(InvocationAdapter.class);

    private final ClassLoader classLoader;

    public InvocationAdapter(File publicDirectory, ClassLoader classLoader) {
        super(publicDirectory.getAbsolutePath());
        this.classLoader = classLoader;
    }

    @Override
    public void service(GrizzlyRequest request, GrizzlyResponse response) {
        MethodCallResult result = result(request);
        writeMethodCallResult(response, result);
    }

    private MethodCallResult result(GrizzlyRequest request) {
        Session session = null;
        MethodCall call = null;
        try {
            call = readMethodCall(request);
            session = call.getSession();
            if (call.isOnTarget()) {
                return call.invoke();
            }
            log.error(call + " failed to find its target" + (session == null ? "" : ", session: " + session));
            return UnknownMethodTarget.INSTANCE;
        } catch (Exception e) {
            log.error(call == null ? "Deserialization of call failed"
                    : "Processing of call " + call + " failed" +
                            (session == null ? "" : ", session: " + session), e);
            return new MethodExceptionThrown(session, e);
        }
    }

    private static void writeMethodCallResult(GrizzlyResponse response,
                                              MethodCallResult result) {
        OutputStream outputStream = openOutputStream(response, result);
        try {
            ObjectOutputStream objectOutputStream = openObjectOutputStream(response, result, outputStream);
            writeObjectResponse(response, result, objectOutputStream);
        } finally {
            Closeables.close(outputStream);
        }
    }

    private static void writeObjectResponse(GrizzlyResponse response, MethodCallResult result, ObjectOutputStream objectOutputStream) {
        try {
            objectOutputStream.writeObject(result);
        } catch (IOException e) {
            throw new RemotingException
                    ("Failed to write to " + response + ", could not write result " + result, e);
        }
    }

    private static ObjectOutputStream openObjectOutputStream(GrizzlyResponse response, MethodCallResult result, OutputStream outputStream) {
        try {
            return new ObjectOutputStream(outputStream);
        } catch (IOException e) {
            throw new RemotingException
                    ("Failed to write object header to " + response + ", could not write result " + result, e);

        }
    }

    private static OutputStream openOutputStream(GrizzlyResponse response, MethodCallResult result) {
        try {
            return response.getOutputStream();
        } catch (IOException e) {
            throw new RemotingException
                    ("Failed to open response stream for " + response + ", could not write result " + result, e);
        }
    }

    private MethodCall readMethodCall(GrizzlyRequest request) {
        Object object = readRequestObject(request);
        if (object == null) {
            throw new RemotingException
                    ("Read null object from " + request);
        }
        if (object instanceof MethodCall) {
            return (MethodCall) object;
        }
        throw new RemotingException
                ("Read unexpected instance " + object + " of unknown " + object.getClass() + " from " + request);
    }

    private Object readRequestObject(GrizzlyRequest request) {
        InputStream inputStream = requestInputStream(request);
        try {
            ObjectInputStream objectInputStream = objectInputStream(request, inputStream);
            return readObjectWithClassLoader(request, objectInputStream);
        } finally {
            Closeables.close(inputStream);
        }
    }

    private Object readObjectWithClassLoader(GrizzlyRequest request, ObjectInputStream objectInputStream) {
        ContextClassLoaderSwitch contextSwitch = new ContextClassLoaderSwitch(classLoader);
        try {
            return objectInputStream.readObject();
        } catch (IOException e) {
            throw new RemotingException("Failed to read method invocation from " + request, e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to read class from " + request, e);
        } finally {
            contextSwitch.revert();
        }
    }

    private static ObjectInputStream objectInputStream(GrizzlyRequest request, InputStream inputStream) {
        try {
            return new ContextClassLoaderObjectInputStream(inputStream);
        } catch (IOException e) {
            throw new RemotingException("Failed to read object header from " + request, e);
        }
    }

    private static InputStream requestInputStream(GrizzlyRequest request) {
        try {
            return request.getInputStream();
        } catch (IOException e) {
            throw new RemotingException("Failed to open " + request, e);
        }
    }

    @Override
    public void afterService(GrizzlyRequest request, GrizzlyResponse response) {
        request.recycle();
        response.recycle();
    }

}
