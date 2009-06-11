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

package vanadis.core.text;

import vanadis.core.lang.VarArgs;
import vanadis.core.properties.PropertySet;
import vanadis.core.properties.PropertySets;

import java.util.Map;

public final class Resolve {

    public static String resolve(String str, Map<String,?> vars) {
        return resolve(str, PropertySets.create(vars));    
    }

    public static String resolve(String str, PropertySet... vars) {
        if (!VarArgs.present(vars)) {
            return str;
        }
        if (!str.contains(L_DEREF)) {
            return str;
        }
        String processed;
        if (str.contains(L_DEREF)) {
            processed = process(str, vars);
        } else {
            processed = str;
        }
        return processed;
    }

    private static final String L_DEREF = "${";

    private static final String R_DEREF = "}";

    private static final int RIGHT_LEN = R_DEREF.length();

    private static final int LEFT_LEN = L_DEREF.length();

    @SuppressWarnings({"AssignmentToForLoopParameter"})
    private static String process(String str, PropertySet... vars) {
        StringBuilder sb = new StringBuilder();
        for (int p = 0; p < str.length();) {
            int left = str.indexOf(L_DEREF, p);
            if (left < 0) {
                sb.append(str.substring(p));
                p = str.length();
            } else {
                sb.append(str.substring(p, left));
                int right = str.indexOf(R_DEREF, left);
                if (right < 0) {
                    throw new IllegalArgumentException
                            ("Unclosed variable:" + str.substring(left));
                } else {
                    p = right + RIGHT_LEN;
                    String variable = str.substring(left + LEFT_LEN, right);
                    appendByProperties(sb, variable, vars);
                }
            }
        }
        return sb.toString();
    }

    private static void appendByProperties(StringBuilder sb,
                                           String variable,
                                           PropertySet[] propertySets) {
        for (PropertySet propertySet : propertySets) {
            if (propertySet.has(String.class, variable)) {
                sb.append(propertySet.getString(variable));
                return;
            }
        }
        sb.append(L_DEREF).append(variable).append(R_DEREF);
    }
}
