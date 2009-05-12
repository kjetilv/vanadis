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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public abstract class AbstractWireSessionable extends AbstractSessionable implements Externalizable {

    private static final long serialVersionUID = -3200960710375785789L;

    protected AbstractWireSessionable() {
        this(null);
    }

    protected AbstractWireSessionable(Session session) {
        super(session);
    }

    @Override
    public void writeExternal(ObjectOutput objectOutput)
            throws IOException {
        Session session = getSession();
        objectOutput.writeUTF(session == null ? "" : session.id());
    }

    @Override
    public void readExternal(ObjectInput objectInput)
            throws IOException, ClassNotFoundException {
        String id = objectInput.readUTF();
        if (id.trim().length() > 0) {
            setSession(new Session(id));
        }
    }

}
