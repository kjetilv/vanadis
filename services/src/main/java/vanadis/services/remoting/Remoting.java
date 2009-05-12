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
package vanadis.services.remoting;

import vanadis.core.io.Location;

import java.io.Closeable;

/**
 * A service for connecting to remote VM's.
 */
public interface Remoting extends Closeable {

    <T> T connect(TargetHandle<T> targetHandle);

    <T> T connect(TargetHandle<T> targetHandle, boolean newSession);

    <T> T connect(ClassLoader classLoader, TargetHandle<T> targetHandle);

    <T> T connect(ClassLoader classLoader, TargetHandle<T> targetHandle, boolean newSession);

    Remoting addLocator(Object locator);

    boolean isEndPoint();

    Location getEndPoint();

    boolean isActive();
}
