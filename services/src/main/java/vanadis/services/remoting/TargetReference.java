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
package net.sf.vanadis.services.remoting;

/**
 * <P>A reference interface allowing lookup of a certain object type T,
 * using a lookup type L.</P>
 *
 * @param <T> Type of target
 * @param <L> Type of locator
 */
public interface TargetReference<T, L> {

    Class<L> getLookupClass();

    T get(L lookup);

    Class<T> getTargetInterface();

    void restoreTargetInterface(ClassLoader classLoader);

    void dispose();

}
