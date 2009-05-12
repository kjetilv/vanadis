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

import vanadis.core.lang.Not;
import vanadis.ext.CommandExecution;
import vanadis.osgi.Context;

class QuitExecution implements CommandExecution {

    private final FrameworkExtender extender;

    QuitExecution(FrameworkExtender extender) {
        this.extender = Not.nil(extender, "extender");
    }

    @Override
    public void exec(String command, String[] args, StringBuilder sb, Context context) {
        extender.close(true);
    }
}
