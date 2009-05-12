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
package vanadis.osgi;

enum CompareOperator {

    EQUAL("=") {
        @Override
        protected boolean match(int result) {
            return result == 0;
        }},

    APPROX("~=") {
        @Override
        protected boolean match(int result) {
            return result == 0;
        }},

    GREATER(">=") {
        @Override
        protected boolean match(int result) {
            return result > 0;
        }},

    LESS("<=") {
        @Override
        protected boolean match(int result) {
            return result < 0;
        }};

    private final String repr;

    CompareOperator(String repr) {
        this.repr = repr;
    }

    public String repr() {
        return repr;
    }

    public boolean compares(Object c1, Object c2) {
        if (c1.getClass().equals(c2.getClass()) && c1 instanceof Comparable<?>) {
            return compare(c1, c2);
        } else {
            throw new IllegalStateException(this + " could not compare " + c1 + " to " + c2);
        }
    }

    @SuppressWarnings({ "RawUseOfParameterizedType", "unchecked" })
    private boolean compare(Object c1, Object c2) {
        Class<? extends Comparable> type = c1.getClass().asSubclass(Comparable.class);
        return match(type.cast(c1).compareTo(type.cast(c2)));
    }

    protected abstract boolean match(int result);

}
