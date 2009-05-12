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

package net.sf.vanadis.util.log;

public final class Logs {

    public static Log get(Class<?> clazz) {
        return new LogWrapper(clazz);
    }

    private static Log get(String logger) {
        return new LogWrapper(logger);
    }

    @SuppressWarnings({ "ThrowableInstanceNeverThrown" })
    public static Log get() {
        StackTraceElement[] stes = new Throwable().getStackTrace();
        return get(stes[1].getClassName());
    }

    private Logs() {}
}
