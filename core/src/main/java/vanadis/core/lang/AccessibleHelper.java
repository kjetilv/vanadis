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
package vanadis.core.lang;

import java.lang.reflect.AccessibleObject;
import java.security.AccessController;
import java.security.PrivilegedAction;

public final class AccessibleHelper {

    public static <T extends AccessibleObject> T openSesame(T t)
            throws SecurityException {
        return AccessController.doPrivileged(new Handwave<T>(t));
    }

    private static final class Handwave<T extends AccessibleObject> implements PrivilegedAction<T> {

        private final T object;

        private Handwave(T object) {
            this.object = object;
        }

        @Override
        public T run() {
            if (!object.isAccessible()) {
                object.setAccessible(true);
            }
            return object;
        }
    }
}
