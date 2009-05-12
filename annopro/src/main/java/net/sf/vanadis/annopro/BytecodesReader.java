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

package net.sf.vanadis.annopro;

import net.sf.vanadis.core.collections.Generic;
import net.sf.vanadis.core.lang.Not;
import org.objectweb.asm.ClassReader;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;

class BytecodesReader extends AbstractReader {

    private final InputStream bytecode;

    private final String targetAnnotation;

    BytecodesReader(InputStream bytecode, String targetAnnotation) {
        this.bytecode = Not.nil(bytecode, "bytecode stream");
        this.targetAnnotation = targetAnnotation;
    }

    @Override
    public Map<Method, List<AnnotationDatum<Method>>> readAllMethods() {
        return Collections.emptyMap();
    }

    @Override
    public Map<Field, List<AnnotationDatum<Field>>> readAllFields() {
        return Collections.emptyMap();
    }

    @Override
    public Map<String, AnnotationDatum<Class<?>>> annotations() {
        Map<String, AnnotationDatum<Class<?>>> annos = Generic.map();
        ClassReader reader = createReader();
        try {
            reader.accept(new ClassAsmVisitor(annos, targetAnnotation), ClassReader.SKIP_CODE);
        } catch (EarlyBreakException ignore) {
        }
        return annos;
    }

    private ClassReader createReader() {
        try {
            return new ClassReader(bytecode);
        } catch (IOException e) {
            throw new IllegalStateException(this + " failed to read bytecode", e);
        }
    }
}
