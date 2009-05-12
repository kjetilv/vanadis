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

package vanadis.lang.piji;

import vanadis.lang.piji.hold.DataHolderFactory;
import vanadis.lang.piji.hold.Holder;
import vanadis.lang.piji.hold.PrimitiveHolder;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class Result implements Serializable {

    private static final long serialVersionUID = 3L;

    private static String stringValueOf(Object value) {
        if (value == null) {
            return null;
        }
        if (value.getClass().isArray()) {
            Class<?> ct = value.getClass().getComponentType();
            List<Object> objects = Arrays.asList((Object[]) value);
            String string;
            if (objects.isEmpty()) {
                string = "";
            } else {
                String listString = objects.toString();
                string = listString.substring(1, listString.length() - 1);
            }
            String size = "[" + objects.size() + "]";
            return ct.getName() + size + " { " + string + " }";
        } else if (value instanceof Collection) {
            String size = "(" + ((Collection<?>) value).size() + ")";
            return size + " " + value;
        } else {
            return String.valueOf(value);
        }
    }

    public static Result ok(Object expression, Object object, long time) {
        return new Result(expression, object, object instanceof PrimitiveHolder, null, time);
    }

    public static Result fail(Object expression, Throwable t, long time) {
        return new Result(expression, null, false, t, time);
    }

    public static Result noResult(Object expression) {
        return new NullResult(expression);
    }

    private static boolean noq(Object v1, Object v2) {
        return (v1 == null && v2 == null) || (v1 != null && v2 != null && v1.equals(v2));
    }

    private static int ha(Object object, int prime) {
        return object == null ? 0 : prime * object.hashCode();
    }

    private final Object value;

    private final Object typedValue;

    private final Holder wrappedValue;

    private String valueString;

    private boolean primitive;

    private final Class<?> type;

    private String typeName;

    private long evalTime;

    private final Throwable throwable;

    private String throwableType;

    private String throwableMessage;

    private String expressionString;

    Result(Object expression, Object object, boolean primitive,
           Throwable throwable, long time) {
        this.expressionString = expression == null
            ? null
            : String.valueOf(expression);

        this.evalTime = time;
        this.primitive = primitive;

        boolean wrapped = object instanceof Holder;

        this.wrappedValue = wrapped ? (Holder) object : null;

        this.value = wrapped ? DataHolderFactory.drop(object) : object;

        this.typedValue = this.value == null
            ? null
            : (wrapped ? this.wrappedValue : this.value);

        this.valueString = stringValueOf(this.value);

        this.type = this.value == null
            ? null
            : (wrapped ? this.wrappedValue.getType() : this.value.getClass());

        this.typeName = this.type == null ? null : this.type.getName();

        this.throwable = throwable;

        this.throwableType = this.throwable == null
            ? null
            : throwable.getClass().getName();

        this.throwableMessage = this.throwable == null
            ? null
            : (this.throwable.getMessage() == null
                ? "[no info given]"
                : this.throwable.getMessage());
    }

    /**
     * The {@link Holder} object if wrapped, otherwise null.  Will be
     * serialized.
     *
     * @return Holder
     */
    public Holder getWrappedValue() {
        return this.wrappedValue;
    }

    /**
     * True iff the result is wrapped, in a {@link Holder}.
     *
     * @return True iff wrapped
     */
    public boolean isWrapped() {
        return this.wrappedValue != null;
    }

    /**
     * The value.  If the result is a primitive, it returns an
     * instance of the standard Java API wrapper class. Will not be
     * serialized.
     *
     * @return Value
     */
    public Object getValue() {
        return this.value;
    }

    /**
     * The toString of the value.
     *
     * @return toString
     */
    public Object getValueString() {
        return this.valueString;
    }

    /**
     * The holder object if wrapped, otherwise the object itself.
     *
     * @return Typed value
     */
    public Object getTypedValue() {
        return this.typedValue;
    }

    /**
     * The class object of the result.  Will not be serialized.
     *
     * @return Type
     */
    public Class<?> getType() {
        return this.type;
    }

    /**
     * The name of the class of the result.
     *
     * @return Type name
     */
    public String getTypeName() {
        return this.typeName;
    }

    /**
     * True iff the result was a primitive value.
     *
     * @return True iff primtive
     */
    public boolean isPrimitive() {
        return this.primitive;
    }

    /**
     * The throwable of a failed result.  Will not be serialized.
     *
     * @return Throwable
     */
    public Throwable getThrowable() {
        return this.throwable;
    }

    public String getThrowableType() {
        return this.throwableType;
    }

    public String getThrowableMessage() {
        return this.throwableMessage;
    }

    /**
     * The time taken to evaluate the result, in milliseconds.
     *
     * @return Eval time
     */
    public long getEvalTime() {
        return this.evalTime;
    }

    /**
     * True iff the result was not a failure.
     *
     * @return True iff ok
     */
    public boolean isOK() {
        return this.throwableType == null;
    }

    private void readObject(ObjectInputStream in)
        throws IOException, ClassNotFoundException {
        this.valueString = (String) in.readObject();
        this.primitive = in.readBoolean();
        this.typeName = (String) in.readObject();
        this.evalTime = in.readLong();
        this.throwableType = (String) in.readObject();
        this.throwableMessage = (String) in.readObject();
        this.expressionString = (String) in.readObject();
    }

    private void writeObject(ObjectOutputStream out)
        throws IOException {
        out.writeObject(valueString);
        out.writeBoolean(primitive);
        out.writeObject(typeName);
        out.writeLong(evalTime);
        out.writeObject(throwableType);
        out.writeObject(throwableMessage);
        out.writeObject(expressionString);
    }

    @Override
    public int hashCode() {
        return 3 + (int) evalTime * 31 + (primitive ? 13 : 17) +
            ha(typeName, 29) + ha(valueString, 11) +
            ha(throwableType, 41) + ha(throwableMessage, 43) +
            ha(expressionString, 47);
    }

    @Override
    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }
        if (object == this) {
            return true;
        }
        if (object instanceof Result) {
            Result r = (Result) object;
            return primitive == r.primitive && evalTime == r.evalTime &&
                noq(valueString, r.valueString) &&
                noq(typeName, r.typeName) &&
                noq(throwableType, r.throwableType) &&
                noq(throwableMessage, r.throwableMessage) &&
                noq(expressionString, r.expressionString);
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        String expr = this.expressionString;
        if (expr != null && expr.length() > 20) {
            expr = expr.substring(0, 17) + "...";
        }

        sb.append("Result[");
        sb.append(expr == null ? "NOOP" : expr);
        sb.append("->");
        if (this.isOK()) {
            sb.append(this.valueString);
            sb.append(":");
            sb.append(this.typeName);
            sb.append(" [");
            sb.append(this.evalTime);
            sb.append("ms]");
        } else {
            sb.append("Failed: ");
            sb.append(this.throwableType);
            sb.append(": ");
            sb.append(this.throwableMessage);
        }
        sb.append("]");
        return sb.toString();
    }

}

