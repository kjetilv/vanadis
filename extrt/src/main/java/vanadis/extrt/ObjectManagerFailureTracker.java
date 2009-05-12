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

package vanadis.extrt;

import vanadis.core.collections.Generic;
import vanadis.core.lang.ToString;
import vanadis.core.lang.VarArgs;
import vanadis.ext.Failure;
import vanadis.ext.ObjectManagerFailures;

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.LinkedList;

final class ObjectManagerFailureTracker implements ObjectManagerFailures {

    private final LinkedList<Failure> failures = Generic.linkedList();

    @Override
    public Iterator<Failure> iterator() {
        return failures.iterator();
    }

    @Override
    public Failure getLatestError() {
        return failures.getLast();
    }

    @Override
    public int getFailureCount() {
        return failures.size();
    }

    @Override
    public boolean hasFailed() {
        return !failures.isEmpty();
    }

    String fail(Throwable cause, Object... msg) {
        String message = compileMessage(msg);
        failures.add(new FailureImpl(message, cause));
        return message;
    }

    private static String compileMessage(Object... msg) {
        if (VarArgs.present(msg)) {
            String msgString = String.valueOf(msg[0]);
            return msg.length == 1 ? msgString : compileMessage(msgString, msg);
        }
        return null;
    }

    private static String compileMessage(String msgString, Object... msg) {
        int argCount = msg.length - 1;
        Object[] args = new Object[argCount];
        System.arraycopy(msg, 1, args, 0, argCount);
        return MessageFormat.format(msgString, args);
    }

    @Override
    public String toString() {
        return ToString.of(this, "failures", failures.size());
    }
}
