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

package net.sf.vanadis.lang.piji;

public class ExprRunnable implements Runnable {

    private final Expression[] exprs;

    private final Context ctx;

    private boolean done;

    private Throwable exception;

    private Object value;

    private final Object lock = new Object();

    public ExprRunnable(Context ctx, Expression[] exprs) {
        this.ctx = ctx;
        this.exprs = exprs;
    }

    public boolean isDone() {
        synchronized (lock) {
            return this.done;
        }
    }

    public boolean isOK() {
        synchronized (lock) {
            return this.done && this.value != null;
        }
    }

    @Override
    public String toString() {
        synchronized (lock) {
            return "ExprRunnable[" + (this.done
                    ? (this.value == null
                    ? "Value:" + this.exception
                    : "Exception:" + this.value)
                    : "running...") + "]";
        }
    }

    @Override
    public void run() {
        Object object = null;
        Throwable oops = null;
        try {
            for (int i = 1; i < this.exprs.length; i++) {
                object = this.exprs[i].evaluate(this.ctx);
            }
        } catch (Throwable e) {
            e.printStackTrace();
            oops = e;
        } finally {
            synchronized (lock) {
                if (oops == null) {
                    this.value = object;
                } else {
                    this.exception = oops;
                }
                this.done = true;
            }
        }
    }

}
