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

package net.sf.vanadis.util.concurrent;

import net.sf.vanadis.core.collections.Generic;
import net.sf.vanadis.core.time.Time;
import net.sf.vanadis.core.time.TimeSpan;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ConcurrentSessions<K, V extends Comparable<V>> implements Sessions<K, V> {

    /**
     * <P>Sessions are indexed on a key.  <strong>This map is the central point of thread
     * synchronization</strong.</P>
     *
     * <P>It holds one entry for each user id that is currently known, and not yet
     * {@link #scavenge() scavenged}.  The entry <em>may</em> be
     * {@link #placeholder the placeholder session}, which
     * means the user session is currently being held and edited by a client.</P>
     *
     * <P>When requesting a user's session, the map is consulted.
     */
    private final ConcurrentMap<K, V> sessions;

    /**
     * These are the known sessions, sorted on edit time.  This means we
     * can always find the oldest session with {@link java.util.SortedSet#first()}.
     * Entries live here until they are {@link #scavenge() scavenged}.
     */
    private final ConcurrentSkipListSet<V> sortedSessions;

    private final ThreadLocal<V> threadSession = new ThreadLocal<V>();

    private final SessionFactory<K, V> factory;

    private final AtomicBoolean closed = new AtomicBoolean();

    private final AtomicInteger activeSessions = new AtomicInteger();

    private final Map<K, Time> lastEdited = Generic.concurrentHashMap();

    private static final TimeSpan RETRY_INTERVAL = TimeSpan.HUNDRED_MS;

    private final V placeholder;

    public ConcurrentSessions(SessionFactory<K, V> factory, int concurrency) {
        this(factory, 1024, 0.75f, concurrency);
    }

    public ConcurrentSessions(SessionFactory<K, V> factory,
                              int capacity,
                              float loadFactor,
                              int concurrency) {
        this.factory = factory;
        this.placeholder = factory.createNullInstance();
        this.sortedSessions = Generic.concurrentSkipListSet();
        this.sessions = Generic.concurrentHashMap(capacity, loadFactor, concurrency);
    }

    @Override
    public V acquire(K key, TimeSpan timeout) {
        failClosed();
        failCurrentSession();
        V newCurrent = retrieve(key, timeout);
        return setCurrent(newCurrent);
    }

    @Override
    public int getActiveSessionsCount() {
        return activeSessions.get();
    }

    @Override
    public void release() {
        failNoCurrentSession();
        V session = clearCurrent();
        K key = factory.key(session);
        lastEdited.put(key, Time.mark());
        sortedSessions.remove(session);
        sortedSessions.add(session);
        boolean wasReplaced = sessions.replace(key, placeholder, session);
        assert wasReplaced : "Found unexpected session in place of " + session;
    }

    @Override
    public int close() {
        closed.set(true);
        return sessions.size();
    }

    @Override
    public int getSessionCount() {
        return sessions.size();
    }

    @Override
    public ScavengeResult<V> scavenge() {
        failCurrentSession();
        V session = pickVictim();
        if (session != null) {
            clearSession(session);
            K key = factory.key(session);
            return new ScavengeResult<V>(sessions.size(), session, lastEdited.get(key));
        }
        return null;
    }

    private void failClosed() {
        if (closed.get()) {
            throw new IllegalStateException(this + " was closed");
        }
    }

    private void failCurrentSession() {
        if (current() != null) {
            throw new IllegalStateException
                    (this + " in " + Thread.currentThread() + " already holds session " + current());
        }
    }

    private void failNoCurrentSession() {
        if (current() == null) {
            throw new IllegalStateException(this + " in " + Thread.currentThread() + " holds no session");
        }
    }

    private V clearCurrent() {
        V session = current();
        if (session == null) {
            throw new IllegalStateException(this + " has no session in " + Thread.currentThread());
        }
        setCurrent(null);
        return session;
    }

    private V setCurrent(V session) {
        threadSession.set(session);
        activeSessions.getAndAdd(session == null ? -1 : 1);
        return session;
    }

    private V current() {
        return threadSession.get();
    }

    private void clearSession(V session) {
        boolean removed = sessions.remove(factory.key(session), placeholder);
        assert removed : this + " tried to remove placeholder for " + session + ", but found other session";
        factory.destroy(session);
    }

    private V pickVictim() {
        while (!sessions.isEmpty()) {
            V session = sortedSessions.pollFirst();
            if (session != null) {
                if (extractedVictim(session)) {
                    return session;
                }
            } else if (closed.get() && !sessions.isEmpty()) {
                try {
                    session = sessions.values().iterator().next();
                } catch (NoSuchElementException ignore) {
                    continue;
                }
                if (extractedVictim(session)) {
                    return session;
                }
            }
        }
        return null;
    }

    private boolean extractedVictim(V session) {
        if (sessions.replace(factory.key(session), session, placeholder)) {
            sortedSessions.remove(session);
            return true;
        }
        return false;
    }

    private V retrieve(K key, TimeSpan timeout) {
        V session = this.sessions.put(key, placeholder);
        if (session == null) { // No such session exists, create a new one
            return factory.create(key);
        }
        if (session == placeholder) { // Session has already been grabbed, for either editing or disposal
            return timeout == null ? null // Give up
                    : retrieveRetry(key, timeout);
        }
        return session; // Session was retrieved
    }

    private V retrieveRetry(final K key, TimeSpan timeout) {
        return timeout.newDeadline().tryEvery(RETRY_INTERVAL, new Callable<V>() {
            @Override
            public V call() {
                return retrieve(key, null);
            }
        });
    }

}
