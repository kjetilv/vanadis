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

/**
 * <P>This is the extender module.  The extender pattern is a pretty common OSGi 'framework' thing;
 * it involves having one bundle that listens for other bundles, and then doing stuff on behalf
 * of these bundles.  This is a pretty sensible pattern, since the alternative involves depending
 * on every bundle doing the right thing, independently.  In short, it means writing the code that
 * does the right thing once, instead of specifying the right thing in a document and then hoping
 * every bundle will comply.  That just doesn't work.</P>
 *
 * <P>This bundle is that extender.  It launches by having its {@link vanadis.ext.BundleActivator
 * bundle activator} launch a {@link vanadis.ext.FrameworkTracker framework tracker}.
 * It listens for bundles, and then pokes around in them for the
 * {@link vanadis.ext.Module} annotation.  Once
 * found, it will register {@link ObjectManagerFactory object manager factories}
 * for each module {@link vanadis.ext.Module#moduleType() type}.
 */
package vanadis.extrt;