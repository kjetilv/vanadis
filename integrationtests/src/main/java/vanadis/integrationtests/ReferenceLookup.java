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
package vanadis.integrationtests;

import vanadis.osgi.Context;
import vanadis.osgi.Filter;
import vanadis.osgi.OSGiUtils;
import vanadis.osgi.Reference;

import java.util.concurrent.Callable;

class ReferenceLookup<T> implements Callable<Reference<T>> {

    private final Context context;

    private final Class<T> serviceInterface;

    private final Filter filter;

    private final boolean forNull;

    private final boolean expectNonNull;

    ReferenceLookup(Context context, Class<T> serviceInterface, Filter filter, boolean forNull) {
        this.context = context;
        this.serviceInterface = serviceInterface;
        this.filter = filter;
        this.forNull = forNull;
        this.expectNonNull = !forNull;
    }

    @Override
    public Reference<T> call() {
        printWait();
        Reference<T> reference = null;
        try {
            reference = context.getReference(serviceInterface, filter);
        } catch (IllegalStateException e) {
            if (OSGiUtils.bundleNoLongerValid(e) && forNull) {
                printOK();
                return reference;
            } else {
                throw e;
            }
        }
        boolean nullRef = reference == null;
        if ((nullRef && forNull) || !nullRef && expectNonNull) {
            printOK();
        }
        return reference;
    }

    private static void printWait() {
        System.out.print(".");
    }

    private static void printOK() {
        System.out.println(" ok!");
    }
}
