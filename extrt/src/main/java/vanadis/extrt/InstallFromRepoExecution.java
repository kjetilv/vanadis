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

import vanadis.blueprints.BundleSpecification;
import vanadis.ext.CommandExecution;
import vanadis.ext.Printer;
import vanadis.osgi.Context;
import vanadis.util.mvn.Coordinate;

import java.net.URI;

class InstallFromRepoExecution implements CommandExecution {

    @Override
    public void exec(String command, String[] args, Printer p, Context context) {
        for (String arg : args) {
            try {
                Coordinate coordinate = Coordinate.at(arg);
                URI uri = coordinate.uriIn(context.getRepo());
                context.register(BundleSpecification.create(uri, 1, null), BundleSpecification.class);
            } catch (Exception e) {
                p.p("Failed to install ").p(arg).p(": ").p(e).cr();
            }
        }
    }
}