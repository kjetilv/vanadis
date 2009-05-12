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
package vanadis.osgi.impl;

import vanadis.core.properties.AbstractPropertySet;
import vanadis.core.properties.PropertySet;
import vanadis.osgi.Context;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Collections;

final class OSGiContextPropertySet extends AbstractPropertySet {

    private final transient Context context;

    private static final long serialVersionUID = 7916227561187126242L;

    OSGiContextPropertySet(Context context) {
        this(context, null);
    }

    private OSGiContextPropertySet(Context context, AbstractPropertySet next) {
        super(next, false);
        this.context = context;
    }

    @Override
    protected Object getLocal(String key) {
        return context.getProperty(key);
    }

    @Override
    protected void setLocal(String key, Object value) {
        throw new IllegalArgumentException(this + " cannot set " + key + " to " + value);
    }

    @Override
    protected OSGiContextPropertySet makeCopy(PropertySet parent, boolean writable) {
        return this;
    }

    @Override
    protected AbstractPropertySet doOrphan() {
        return new OSGiContextPropertySet(context, null);
    }

    @Override
    public PropertySet expand(PropertySet... variables) {
        return this;
    }

    @Override
    public int size() {
        return context.getPropertySet().size();
    }

    @Override
    public Collection<String> getPropertyNames() {
        return Collections.emptySet();
    }

    private void writeObject(ObjectOutputStream oos) {
        throw new IllegalStateException(this + " will not serialize!");
    }

    private void readObject(ObjectInputStream ois) {
        throw new IllegalStateException(this + " will not serialize!");
    }

}
