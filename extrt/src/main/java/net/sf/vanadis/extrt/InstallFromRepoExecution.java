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
package net.sf.vanadis.extrt;

import net.sf.vanadis.blueprints.BundleSpecification;
import net.sf.vanadis.core.system.VM;
import net.sf.vanadis.ext.CommandExecution;
import net.sf.vanadis.osgi.Context;
import net.sf.vanadis.util.mvn.Coordinate;

import java.net.URI;

class InstallFromRepoExecution implements CommandExecution {

    @Override
    public void exec(String command, String[] args, StringBuilder sb, Context context) {
        for (String arg : args) {
            try {
                Coordinate coordinate = Coordinate.at(arg);
                URI uri = coordinate.uriIn(context.getRepo());
                context.register(BundleSpecification.create(uri, 1, null), BundleSpecification.class);
            } catch (Exception e) {
                sb.append("Failed to install ").append(arg).append(": ").append(e).append(VM.LN);
            }
        }
    }
}