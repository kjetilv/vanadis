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

package vanadis.core.lang;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public abstract class GraphIterable<E> implements Iterable<E> {

    private final List<E> nodes;

    protected GraphIterable(E node) {
        this.nodes = node == null ? Collections.<E>emptyList() : enumerate(node);
    }

    @Override
    public Iterator<E> iterator() {
        return nodes.iterator();
    }

    private List<E> enumerate(E node) {
        List<E> es = new ArrayList<E>();
        enumerate(es, node);
        return es;
    }

    private void enumerate(List<E> es, E node) {
        if (!es.contains(node)) {
            es.add(node);
        }
        Iterable<E> nextList = iterable(node);
        if (nextList == null || !nextList.iterator().hasNext())  {
            return;
        }
        for (E next : nextList) {
            enumerate(es, next);
        }
    }

    protected abstract Iterable<E> iterable(E node);
}