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
package net.sf.vanadis.remoting;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Comparator;

public class MethodComparator implements Comparator<Method>, Serializable {

    private static final long serialVersionUID = -7791866772101280273L;

    @Override
    public int compare(Method m1, Method m2) {
        if (m1.equals(m2)) {
            return 0;
        }
        int nameCompare = m1.getName().compareTo(m2.getName());
        return nameCompare != 0 ? nameCompare
                : compareTypes(m1.getParameterTypes(), m2.getParameterTypes());
    }

    private static int compareTypes(Class<?>[] ca1, Class<?>[] ca2) {
        int length = ca1.length;
        int diff = length - ca2.length;
        if (diff == 0) {
            for (int i = 0; i < length; i++) {
                int namecomp = ca1[i].getName().compareTo(ca2[i].getName());
                if (namecomp != 0) {
                    return namecomp;
                }
            }
            return 0;
        } else {
            return diff < 0 ? -1 : 1;
        }
    }

}
