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
package net.sf.vanadis.remoting;

class ArrayTargetReference<T> extends AbstractTargetReference<T, ArrayLocator> {

    private final int i;

    private static final long serialVersionUID = 3668912854418454787L;

    ArrayTargetReference(Class<T> clazz, int i) {
        super(clazz, ArrayLocator.class);
        this.i = i;
    }

    int getI() {
        return i;
    }

    @Override
    public Object retrieve(ArrayLocator locator) {
        return locator.get(getTargetInterfaceName(), i);
    }

}
