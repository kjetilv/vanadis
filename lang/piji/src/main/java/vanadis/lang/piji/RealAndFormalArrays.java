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

package net.sf.vanadis.lang.piji;

final class RealAndFormalArrays {

    private final Class<?>[] signature;

    private final Object[] realArguments;

    RealAndFormalArrays(Reflector ref,
                        int offset, Expression[] args, Context ctx)
            throws Throwable {
        int argumentsCount = args.length - offset;
        Class<?>[] signature = new Class[argumentsCount];
        Object[] realArguments = new Object[argumentsCount];
        for (int i = 0; i < argumentsCount; i++) {
            Object evaluated = args[i + offset].evaluate(ctx);
            signature[i] = Reflector.resolveType(evaluated, true);
            realArguments[i] = Reflector.resolveObject(evaluated);
        }
        this.signature = signature;
        this.realArguments = realArguments;
    }

    public Class<?>[] getSignature() {
        return signature;
    }

    public Object[] getRealArguments() {
        return realArguments;
    }
}

