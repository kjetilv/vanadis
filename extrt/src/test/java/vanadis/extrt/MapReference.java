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

package vanadis.extrt;

import vanadis.osgi.Reference;
import vanadis.osgi.ServiceProperties;

import java.util.Map;

@SuppressWarnings({"RawUseOfParameterizedType"})
public class MapReference implements Reference<Map> {

    private final Map<?, ?> map;

    public MapReference(Map<?, ?> map) {
        this.map = map;
    }

    public boolean isClosed() {
        return closed;
    }

    private boolean closed;

    public boolean isUngotten() {
        return ungotten;
    }

    private boolean ungotten;

    public Class<Map> getServiceInterface() {
        return Map.class;
    }

    @Override
    public Map<?, ?> getService() {
        return map;
    }

    @Override
    public Object getRawService() {
        return map;
    }

    @Override
    public boolean unget() {
        ungotten = true;
        return true;
    }

    @Override
    public long getServiceId() {
        return 0;
    }

    @Override
    public String getServicePid() {
        return null;
    }

    @Override
    public ServiceProperties<Map> getServiceProperties() {
        return ServiceProperties.create(Map.class);
    }

    @Override
    public void close() {
        closed = true;
    }
}
