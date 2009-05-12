/*
 * Copyright 2008 Kjetil Valstadsve
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
package vanadis.modules.httpprovider;

import vanadis.core.io.Closeables;
import vanadis.core.lang.ContextClassLoaderSwitch;
import vanadis.core.reflection.ContextClassLoaderObjectInputStream;
import vanadis.remoting.AbstractHandler;
import vanadis.remoting.MethodCall;
import vanadis.remoting.MethodCallResult;
import vanadis.services.remoting.RemotingException;
import vanadis.services.remoting.TargetHandle;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class Handler<T> extends AbstractHandler {

    private final URL url;

    private final ClassLoader classLoader;

    public Handler(TargetHandle<T> targetHandle, ClassLoader classLoader) {
        super(targetHandle);
        this.classLoader = classLoader;
        this.url = targetHandle.getLocation().toHttpUrl();
    }

    private HttpURLConnection connect() {
        HttpURLConnection connection;
        try {
            connection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            throw new IllegalArgumentException
                (this + " got bad location: " + url, e);
        }
        connection.setDoOutput(true);
        return connection;
    }

    private HttpURLConnection write(MethodCall call, HttpURLConnection connection) {
        connection.setDoOutput(true);
        try {
            connection.connect();
        } catch (IOException e) {
            throw new RemotingException(this + " failed to connect " + connection, e);
        }
        OutputStream outputStream;
        try {
            outputStream = connection.getOutputStream();
        } catch (IOException e) {
            throw new RemotingException(this + " failed to open output on " + connection, e);
        }
        ObjectOutputStream objectOutputStream;
        try {
            objectOutputStream = new ObjectOutputStream(outputStream);
        } catch (IOException e) {
            throw new RemotingException(this + " failed to write header to " + connection, e);
        }
        try {
            objectOutputStream.writeObject(call);
        } catch (IOException e) {
            throw new RemotingException(this + " failed to write " + call + " to " + connection, e);
        }
        Closeables.close(objectOutputStream);
        return connection;
    }

    @Override
    protected MethodCallResult invoke(MethodCall methodCall) {
        HttpURLConnection connection = connect();
        write(methodCall, connection);
        return read(connection);
    }

    private MethodCallResult read(HttpURLConnection connection) {
        InputStream inputStream;
        try {
            inputStream = connection.getInputStream();
        } catch (IOException e) {
            throw new RemotingException
                (this + " failed to open input from " + connection, e);
        }
        ObjectInputStream objectInputStream;
        try {
            objectInputStream = new ContextClassLoaderObjectInputStream(inputStream);
        } catch (IOException e) {
            throw new RemotingException
                (this + " failed to read object headers from " + connection, e);
        }
        ContextClassLoaderSwitch classLoaderSwitch = new ContextClassLoaderSwitch(getClass().getClassLoader());
        try {
            MethodCallResult.setThreadLocalClassLoader(classLoader);
            try {
                return (MethodCallResult) objectInputStream.readObject();
            } catch (Exception e) {
                throw new RemotingException
                    (this + " failed to read result from " + connection, e);
            } finally {
                MethodCallResult.setThreadLocalClassLoader(null);
            }
        } finally {
            classLoaderSwitch.revert();
        }
    }

}
