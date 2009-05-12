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
package net.sf.vanadis.launcher;

import net.sf.vanadis.core.lang.Not;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import java.io.PrintStream;

class ReferenceVanishWaiter extends Waiter {

    private final String type;

    private final String filter;

    private final LaunchResult launchResult;

    ReferenceVanishWaiter(String type, String filter, int impatience, PrintStream stream, LaunchResult launchResult) {
        super(0, impatience, stream);
        this.type = type;
        this.filter = filter;
        this.launchResult = Not.nil(launchResult, "launch result");
    }

    @Override
    protected int getCount() {
        try {
            ServiceReference[] references = launchResult.getBundleContext().getServiceReferences(type, filter);
            return references == null ? 0 : references.length;
        } catch (InvalidSyntaxException e) {
            throw new RuntimeException("Unexpected null-filter exception!", e);
        }
    }
}
