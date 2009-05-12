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
package net.sf.vanadis.remoting;

import java.text.MessageFormat;

public abstract class AbstractSessionable {

    private Session session;

    protected AbstractSessionable() {
        this(null);
    }

    protected AbstractSessionable(Session session) {
        this.session = session;
    }

    public Session getSession() {
        return session;
    }

    protected AbstractSessionable setSession(Session session) {
        this.session = session;
        return this;
    }

    protected boolean isSessioned() {
        return session != null;
    }

    protected void adoptOrVerifySession(AbstractSessionable sessionable) {
        Session otherSession = sessionable.getSession();
        if (this.isSessioned()) {
            if (otherSession != null && !getSession().equals(otherSession)) {
                throw new IllegalStateException
                        (MessageFormat.format
                                ("{0} is session {2}, cannot swich session to {3} owned by {1}",
                                 this, sessionable,
                                 this.getSession(), otherSession));
            }
        } else {
            if (otherSession != null) {
                session = otherSession;
            }
        }

    }
}
