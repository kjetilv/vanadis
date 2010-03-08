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

package vanadis.annopro;

import org.objectweb.asm.*;

import java.util.Map;

class ClassAsmVisitor implements ClassVisitor {

    private final Map<String, AnnotationDatum<Class<?>>> data;

    private final String targetAnnotation;

    private AnnotationMapper mapper;

    ClassAsmVisitor(Map<String, AnnotationDatum<Class<?>>> data, String targetAnnotation, AnnotationMapper mapper) {
        this.data = data;
        this.targetAnnotation = targetAnnotation;
        this.mapper = mapper;
    }

    @Override
    public void visit(int i, int i1, String s, String s1, String s2, String[] strings) {
    }

    @Override
    public void visitSource(String s, String s1) {
    }

    @Override
    public void visitOuterClass(String s, String s1, String s2) {
    }

    @Override
    public AnnotationVisitor visitAnnotation(String name, boolean unused) {
        String type = Bytedecoder.className(name);
        return new AnnotationAsmVisitor<Class<?>>(type, type, data, null, null, targetAnnotation, mapper);
    }

    @Override
    public void visitAttribute(Attribute attribute) {
    }

    @Override
    public void visitInnerClass(String s, String s1, String s2, int i) {
    }

    @Override
    public FieldVisitor visitField(int i, String s, String s1, String s2, Object o) {
        return null;
    }

    @Override
    public MethodVisitor visitMethod(int i, String s, String s1, String s2, String[] strings) {
        return null;
    }

    @Override
    public void visitEnd() {
    }

    public static boolean is(String desc, Class<?> type) {
        return desc.equals("L" + type.getName().replace('.', '/') + ";");
    }
}
