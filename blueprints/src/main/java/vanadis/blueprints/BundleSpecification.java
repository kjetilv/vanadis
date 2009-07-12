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
import vanadis.mvn.Coordinate;

import java.net.MalformedURLException;
import java.net.URI;

public final class BundleSpecification extends AbstractSpecification {

    public static BundleSpecification createFixed(URI uri, Integer startLevel,
                                                  PropertySet propertySet) {
        return new BundleSpecification(null, null, Not.nil(uri, "uri"),
                                       startLevel, propertySet,
                                       null, null);
    }

    public static BundleSpecification createFixed(URI uri, Integer startLevel,
                                                  PropertySet propertySet, Boolean globalProperties) {
        return new BundleSpecification(null, null, Not.nil(uri, "uri"),
                                       startLevel, propertySet,
                                       globalProperties, null);
    }

    public static BundleSpecification create(URI repo, Coordinate coordinate, Integer startLevel,
                                             PropertySet propertySet,
                                             Boolean globalProperties, String configPropertiesPid) {
        return new BundleSpecification(repo, coordinate, null,
                                       startLevel, propertySet,
                                       globalProperties, configPropertiesPid);
    }

    private final URI repo;

    private final Coordinate coordinate;

    private final URI directUri;

    private final Integer startLevel;

    private final String urlString;

    private final String uriString;

    private BundleSpecification(URI repo, Coordinate coordinate, URI directUri, Integer startLevel,
                                PropertySet propertySet,
                                Boolean globalProperties, String configPropertiesPid) {
        super(propertySet, globalProperties, configPropertiesPid);
        this.repo = repo;
        this.coordinate = coordinate;
        this.directUri = directUri;
        this.startLevel = startLevel;
        this.urlString = urlString(this.directUri);
        this.uriString = uriString(this.directUri);
    }

    public BundleSpecification resolve(BundleResolver resolver) {
        Not.nil(resolver, "bundle resolver");
        URI uri = resolver.resolve(this);
        return uri == null ? null
                : new BundleSpecification(null, coordinate, uri, startLevel, getPropertySet(),
                                          isGlobalProperties(), null);
    }

    public boolean sameVersion(BundleSpecification bundleSpecification) {
        return coordinate != null && bundleSpecification.coordinate != null && coordinate.sameVersion(
                bundleSpecification.coordinate);
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public URI getRepo() {
        return repo;
    }

    public String getUrlString() {
        return urlString;
    }

    public String getUriString() {
        return uriString;
    }

    public URI getUri() {
        return directUri;
    }

    public String getBase() {
        return directUri == null ? coordinate.getBase() : directUri.toASCIIString();
    }

    public Integer getStartLevel() {
        return startLevel;
    }

    public boolean isFile() {
        return directUri.getScheme().toLowerCase().startsWith("file");
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
        return ToString.of(this, coordinate, "uri", directUri, "level", startLevel);
    }

    @Override
    public int hashCode() {
        return EqHc.hc(coordinate, directUri, startLevel);
    }

    @Override
    public boolean equals(Object obj) {
        BundleSpecification bundleSpecification = EqHc.retyped(this, obj);
        return bundleSpecification != null && EqHc.eq(coordinate, bundleSpecification.coordinate,
                                                      directUri, bundleSpecification.directUri);
    }

    private static final long serialVersionUID = -8645664267297894852L;
}
