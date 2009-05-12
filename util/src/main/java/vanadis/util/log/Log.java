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

package vanadis.util.log;

public interface Log {

    void info(String msg);

    void info(String msg, Throwable throwable);

    void debug(String msg);

    void debug(String msg, Throwable throwable);

    void warn(String msg);

    void warn(String msg, Throwable throwable);

    void error(String msg);

    void error(String msg, Throwable throwable);

    void fatal(String msg);

    void fatal(String msg, Throwable throwable);

    void log(String level, String msg);

    void log(String level, String msg, Throwable throwable);

    boolean isDebug();

    boolean isInfo();

    boolean isWarn();

    boolean isError();

    boolean isFatal();
}
