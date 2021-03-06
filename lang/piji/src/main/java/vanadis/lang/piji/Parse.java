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

package vanadis.lang.piji;

import java.io.InputStream;
import java.io.Reader;
import java.util.List;

public interface Parse {

    Expression parse(String string)
        throws ParseException;

    Expression parse(InputStream stream)
        throws ParseException;

    Expression parse(Reader reader)
        throws ParseException;

    List<Expression> parseAll(String string)
        throws ParseException;

    List<Expression> parseAll(InputStream stream)
        throws ParseException;

    List<Expression> parseAll(Reader reader)
        throws ParseException;

}
