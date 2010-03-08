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

package vanadis.annopro;

import org.objectweb.asm.AnnotationVisitor;
import vanadis.core.collections.Generic;
import vanadis.core.lang.ToString;
import vanadis.core.properties.PropertySet;
import vanadis.core.properties.PropertySets;

import java.util.List;
import java.util.Map;

class AnnotationAsmVisitor<E>
    implements AnnotationVisitor {

    private final String name;

    private final String type;

    private final Map<String, AnnotationDatum<E>> hostData;

    private final List<AnnotationDatum<E>> hostArray;

    private final PropertySet propertySet;

    private final PropertySet hostPropertySet;

    private final String targetAnnotation;

    private final AnnotationMapper mapper;

    private boolean gotWhatWeNeeded;

    AnnotationAsmVisitor(String name, String type,
                         Map<String, AnnotationDatum<E>> hostData,
                         List<AnnotationDatum<E>> hostArray,
                         PropertySet propertySet,
                         String targetAnnotation,
                         AnnotationMapper mapper) {
        this.name = name;
        this.type = type;
        this.hostData = hostData;
        this.hostArray = hostArray;
        this.hostPropertySet = propertySet;
        this.targetAnnotation = targetAnnotation;
        this.mapper = mapper;
        if (hostData != null) {
            assert hostArray == null && hostPropertySet == null;
        } else if (hostArray != null) {
            assert hostPropertySet == null;
        }
        this.propertySet = PropertySets.create();
    }

    @Override
    public void visit(String name, Object value) {
        propertySet.set(name, value);
    }

    @Override
    public void visitEnum(String name, String desc, String value) {
        propertySet.set(name, value);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String name, String desc) {
        String type = Bytedecoder.className(desc);
        if (targetAnnotation != null && targetAnnotation.equals(type)) {
            gotWhatWeNeeded = true;
        }
        return name != null
            ? new AnnotationAsmVisitor<E>(name, type, null, null, propertySet, targetAnnotation, mapper)
            : new AnnotationAsmVisitor<E>(null, type, null, hostArray, null, targetAnnotation, mapper);
    }

    @Override
    public AnnotationVisitor visitArray(String name) {
        List<AnnotationDatum<E>> collection = Generic.list();
        propertySet.set(name, collection);
        return new AnnotationAsmVisitor<E>(name, type, null, collection, null, targetAnnotation, mapper);
    }

    @Override
    public void visitEnd() {
        AnnotationDatum<E> datum = AnnotationDatum.create(type, propertySet, mapper);
        if (hostArray != null) {
            if (name == null) {
                hostArray.add(datum);
            }
        } else if (hostPropertySet != null) {
            hostPropertySet.set(name, datum);
        } else if (hostData != null) {
            hostData.put(name, datum);
        } else {
            throw new IllegalStateException(this + " has no data, array or host properties");
        }
        if (gotWhatWeNeeded) {
            throw new EarlyBreakException();
        }
    }

    @Override
    public String toString() {
        return ToString.of(this, name,
                           "type", type,
                           "data", hostData,
                           "arrayData", hostArray,
                           "propertySet", propertySet);
    }
}
