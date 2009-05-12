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

package net.sf.vanadis.lang.piji.fun;

import net.sf.vanadis.core.collections.EnumerationIterator;
import net.sf.vanadis.core.lang.EntryPoint;
import net.sf.vanadis.lang.piji.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;

@EntryPoint("Reflection")
public class ForEachFunction extends AbstractFunction {

    @EntryPoint("Reflection")
    public ForEachFunction(Context ctx) {
        super("<symbol> <collections> <expression>*", true, 3, ctx);
    }

    @Override
    public Object apply(Context context, Expression[] args)
            throws Throwable {
        checkArgumentCount(args);

        Object possibleCollection = args[2].evaluate(context);
        if (possibleCollection == null) {
            return null;
        }

        Iterator<?> iterator;
        if (possibleCollection instanceof Collection<?>) {
            iterator = ((Collection<?>) possibleCollection).iterator();
        } else if (possibleCollection instanceof Iterator) {
            iterator = (Iterator<?>) possibleCollection;
        } else if (possibleCollection instanceof Enumeration) {
            iterator = EnumerationIterator.create((Enumeration<?>) possibleCollection);
        } else if (possibleCollection.getClass().isArray()) {
            iterator = Arrays.asList((Object[]) possibleCollection).iterator();
        } else {
            throw new BadArgumentException("Second argument must be " + Collection.class + ", " +
                    Iterator.class + " or array class, now is " +
                    possibleCollection + " of public class " +
                    possibleCollection.getClass());
        }

        Object value = null;
        Symbol symbol = checkSymbol(args[1]);
        Context forEachContext = new Context(context);
        while (iterator.hasNext()) {
            forEachContext.bind(symbol, iterator.next());
            for (int i = 3; i < args.length; i++) {
                value = args[i].evaluate(forEachContext);
            }
        }
        return value;
    }

}
