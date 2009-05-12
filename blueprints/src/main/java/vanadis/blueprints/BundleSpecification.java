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
package vanadis.blueprints;

import vanadis.core.lang.EqHc;
import vanadis.core.lang.Not;
import vanadis.core.lang.ToString;
import vanadis.core.properties.PropertySet;
import vanadis.util.mvn.Coordinate;

import java.net.MalformedURLException;
import java.net.URI;

public final class BundleSpecification extends AbstractSpecification {

    public static BundleSpecification create(URI uri) {
        return new BundleSpecification(uri, null, null, null);
    }

    public static BundleSpecification create(URI uri, Integer startLevel) {
        return new BundleSpecification(uri, startLevel, null, null);
    }

    public static BundleSpecification create(URI uri, Integer startLevel, PropertySet propertySet) {
        return new BundleSpecification(uri, startLevel, propertySet, null);
    }

    public static BundleSpecification create(URI uri, Integer startLevel,
                                             PropertySet propertySet, Boolean globalProperties) {
        return new BundleSpecification(uri, startLevel, propertySet, globalProperties);
    }

    public static BundleSpecification create(Coordinate coordinate) {
        return new BundleSpecification(coordinate, null, null, null);
    }

    public static BundleSpecification create(Coordinate coordinate, Integer startLevel) {
        return new BundleSpecification(coordinate, startLevel, null, null);
    }

    public static BundleSpecification create(Coordinate coordinate, Integer startLevel, PropertySet propertySet) {
        return new BundleSpecification(coordinate, startLevel, propertySet, null);
    }

    public static BundleSpecification create(Coordinate coordinate, Integer startLevel,
                                             PropertySet propertySet, Boolean globalProperties) {
        return new BundleSpecification(coordinate, startLevel, propertySet, globalProperties);
    }

    private final Coordinate coordinate;

    private final URI uri;

    private final Integer startLevel;

    private final String urlString;

    private String uriString;

    private BundleSpecification(URI uri, Integer startLevel,
                                PropertySet properties, Boolean globalProperties) {
        this(null, Not.nil(uri, "uri"), startLevel,
             properties, globalProperties);
    }

    private BundleSpecification(Coordinate coordinate, Integer startLevel,
                                PropertySet properties, Boolean globalProperties) {
        this(Not.nil(coordinate, "coordinate"), null, startLevel,
             properties, globalProperties);
    }

    private BundleSpecification(Coordinate coordinate, URI uri, Integer startLevel,
                                PropertySet propertySet, Boolean globalProperties) {
        super(propertySet, globalProperties);
        this.coordinate = coordinate;
        this.uri = uri;
        this.startLevel = startLevel;
        this.urlString = urlString(this.uri);
        this.uriString = uriString(this.uri);
    }

    public BundleSpecification uriIn(URI root) {
        return new BundleSpecification
                (coordinate, coordinate.uriIn(Not.nil(root, "root")), startLevel, getPropertySet(),
                 isGlobalProperties());
    }

    public boolean sameVersion(BundleSpecification bundleSpecification) {
        return coordinate != null && bundleSpecification.coordinate != null && coordinate.sameVersion(
                bundleSpecification.coordinate);
    }

    public String getUrlString() {
        return urlString;
    }

    public String getUriString() {
        return uriString;
    }

    public URI getUri() {
        return uri;
    }

    public String getBase() {
        return uri == null ? coordinate.getBase() : uri.toASCIIString();
    }

    public Integer getStartLevel() {
        return startLevel;
    }

    public boolean isFile() {
        return uri.getScheme().toLowerCase().startsWith("file");
    }

    private static String uriString(URI uri) {
        return uri == null ? null : uri.toASCIIString();
    }

    private static String urlString(URI uri) {
        if (uri == null) {
            return null;
        }
        try {
            return uri.toURL().toExternalForm();
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid URI: " + uri, e);
        }
    }

    @Override
    public String toString() {
        return ToString.of(this, coordinate, "uri", uri, "level", startLevel);
    }

    @Override
    public int hashCode() {
        return EqHc.hc(coordinate, uri, startLevel);
    }

    @Override
    public boolean equals(Object obj) {
        BundleSpecification bundleSpecification = EqHc.retyped(this, obj);
        return bundleSpecification != null && EqHc.eq(coordinate, bundleSpecification.coordinate,
                                                      uri, bundleSpecification.uri);
    }

    private static final long serialVersionUID = -8645664267297894852L;
}
