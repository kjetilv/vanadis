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
package vanadis.deployer;

import vanadis.core.collections.Generic;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

final class DiscoveredUrisImpl implements DiscoveredUris {

    private final Collection<URI> news;

    private final Collection<URI> updatedURLs;

    private final Collection<URI> removedURLs;

    private static final Set<URI> NO_URLS = Collections.emptySet();

    DiscoveredUrisImpl(Collection<URI> news, Collection<URI> updates, Collection<URI> removes) {
        this.removedURLs = removes == null ? NO_URLS : removes;
        this.news = news == null ? NO_URLS : news;
        this.updatedURLs = updates == null ? NO_URLS : updates;
    }

    @Override
    public Collection<URI> news() {
        return news;
    }

    @Override
    public Collection<URI> updatesAndRemoves() {
        return added(updatedURLs, removedURLs);
    }

    @Override
    public Collection<URI> newsAndUpdates() {
        return added(news, updatedURLs);
    }

    @Override
    public Collection<URI> updates() {
        return updatedURLs;
    }

    @Override
    public Collection<URI> removes() {
        return removedURLs;
    }

    private static <T> Collection<T> added(Collection<T> one, Collection<T> two) {
        Collection<T> urls = Generic.set(one.size() + two.size());
        urls.addAll(one);
        urls.addAll(two);
        return urls;
    }

}
