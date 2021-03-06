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

package vanadis.lang.piji;

import java.io.File;
import java.io.Reader;
import java.io.Writer;

public interface PijiConsole {

    interface Listener {

        void evalException(Exception e);

        void evalEnded(Object value);

        void evalStarting();

    }

    void clearInput();

    void clearValues();

    void clearErrors();

    void reloadClasses();

    void writeInputTo(Writer writer);

    void setFile(File file);

    File getFile();

    void readInputFrom(Reader reader, boolean insert);

    int getInputCaret();

    void setInputCaret(int i);

    void addListener(Listener listener);

    void removeListener(Listener listener);

}
