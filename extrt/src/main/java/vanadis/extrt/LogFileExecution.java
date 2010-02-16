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

package vanadis.extrt;

import vanadis.ext.CommandExecution;
import vanadis.common.text.Printer;
import vanadis.osgi.Context;
import vanadis.osgi.Filter;
import vanadis.osgi.Filters;
import vanadis.osgi.Reference;

import java.io.File;

final class LogFileExecution implements CommandExecution {

    private static final String VANADIS_LOGFILE = "vanadis.logfile";

    private static final Filter LOGFILE_FILTER = Filters.isTrue(VANADIS_LOGFILE);

    @Override
    public void exec(String command, String[] args, Printer p, Context context) {
        Reference<File> reference = context.getReference(File.class, LOGFILE_FILTER);
        if (reference != null) {
            try {
                File file = reference.getService();
                p.p(file.getAbsoluteFile().toURI().toASCIIString());
            } finally {
                reference.unget();
            }
        }
    }
}
