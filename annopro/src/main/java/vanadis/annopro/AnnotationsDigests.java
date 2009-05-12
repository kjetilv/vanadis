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

import vanadis.core.lang.Not;

import java.io.InputStream;

public class AnnotationsDigests {

    public static AnnotationsDigest createFromInstance(Object object) {
        return createFromType(Not.nil(object, "object").getClass());
    }

    public static AnnotationsDigest createFromType(Class<?> clazz) {
        return new AnnotationsDigestsImpl(Not.nil(clazz, "class"), false);
    }

    public static AnnotationsDigest createFromStream(InputStream bytecode) {
        return new AnnotationsDigestsImpl(Not.nil(bytecode, "bytecode stream"));
    }

    public static AnnotationsDigest createFromStream(InputStream bytecode, String targetAnnotation) {
        return new AnnotationsDigestsImpl(Not.nil(bytecode, "bytecode stream"),
                                          Not.nil(targetAnnotation, "target annotation"));
    }

    public static AnnotationsDigest createFullFromInstance(Object object) {
        return createFullFromType(Not.nil(object, "object").getClass());
    }

    public static AnnotationsDigest createFullFromType(Class<?> clazz) {
        return new AnnotationsDigestsImpl(Not.nil(clazz, "class"), true);
    }
}
