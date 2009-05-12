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

package net.sf.vanadis.core.reflection;

import net.sf.vanadis.core.lang.VarArgs;

import java.lang.reflect.Method;

public final class GetNSet {

    public static String getterName(String property) {
        return "get" + upcased(property);
    }

    public static String booleanGetterName(String property) {
        return "is" + upcased(property);
    }

    public static String setterName(String property) {
        return "set" + upcased(property);
    }

    private static String upcased(String property) {
        return property.substring(0, 1).toUpperCase() + property.substring(1);
    }

    public static String getProperty(Method method) {
        return getProperty(method.getName());
    }

    public static String setProperty(Method method) {
        return setProperty(method.getName());
    }

    public static String property(Method method) {
        return property(method.getName());
    }

    public static String getProperty(String methodName) {
        return resolveByPrefix(methodName, true, "get", "is");
    }

    public static String setProperty(String methodName) {
        return resolveByPrefix(methodName, true, "set");
    }

    public static String property(String methodName) {
        return resolveByPrefix(methodName, true, "set", "get", "is");
    }

    public static String resolveByPrefix(String base, String... prefices) {
        return resolveByPrefix(base, false, prefices);
    }

    public static String resolveByPrefix(String base, boolean downcase, String... prefices) {
        if (VarArgs.present(prefices)) {
            for (String prefix : prefices) {
                String suffix = suffix(base, downcase, prefix);
                if (suffix != null) {
                    return suffix;
                }
            }
        }
        return base;
    }

    private static String suffix(String base, boolean downcase, String prefix) {
        int len = prefix.length();
        if (base.length() > len && base.startsWith(prefix) && Character.isUpperCase(base.charAt(len))) {
            String sub = base.substring(len);
            return downcase ? sub.substring(0, 1).toLowerCase() + sub.substring(1) : sub;
        }
        return null;
    }

    private GetNSet() { }
}
