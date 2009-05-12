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

package net.sf.vanadis.util.xml;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Iterator;
import java.util.NoSuchElementException;

class Elements implements Iterable<Element> {

    private final NodeList childNodes;

    Elements(NodeList childNodes) {
        this.childNodes = childNodes;
    }

    @Override
    public Iterator<Element> iterator() {
        return new ElementIterator();
    }

    private class ElementIterator implements Iterator<Element> {

        private int i = -1;

        private final int bound;

        private ElementIterator() {
            bound = childNodes.getLength();
            fastForwardToNextElement();
        }

        private void fastForwardToNextElement() {
            while (hasNext()) {
                i++;
                if (childNodes.item(i) instanceof Element) {
                    return;
                }
            }
        }

        @Override
        public boolean hasNext() {
            return bound > i;
        }

        @Override
        public Element next() {
            if (hasNext()) {
                Node node = childNodes.item(i);
                fastForwardToNextElement();
                return (Element) node;
            }
            throw new NoSuchElementException();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
