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

package vanadis.lang.piji.hold;

/**
 * @author Kjetil Valstadsve
 */
public final class ReferenceHolder extends AbstractDataHolder {

    private final Object object;

    @Override
    public final Object getObject() {
        return this.object;
    }

    private final Class<?> type;

    @Override
    public Class<?> getType() {
        return this.type;
    }

    public final Number getNumber() {
        return (Number) this.object;
    }

    public ReferenceHolder(Object object, Class<?> type) {
        if (object == null || type == null) {
            throw new IllegalArgumentException
                    ("Null values illagel, object=" + object + ", type=" + type);
        }
        this.object = object;
        if (type.isInstance(object)) {
            this.type = type;
        } else {
            throw new IllegalArgumentException
                    (this + " cannot set type of " + object + " to " + type +
                            ", classloaders: " + object.getClass().getClassLoader() +
                            " and " + type.getClassLoader());
        }
    }

    @Override
    public String toValueString() {
        return String.valueOf(this.object);
    }

}
