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

package net.sf.vanadis.launcher;

public class SystemPackages {

    public static final String COVERAGE =
            "com.vladium.emma.rt,com.intellij.rt.coverage.data,com.intellij.rt.coverage,com.intellij.rt";

    public static final String PROFILING =
            "com.jprofiler.agent,org.netbeans.lib.profiler.server";

    public static final String UTIL =
            "net.sf.vanadis.core.collections," +
                    "net.sf.vanadis.core.lang," +
                    "net.sf.vanadis.core.properties," +
                    "net.sf.vanadis.core.system," +
                    "net.sf.vanadis.core.io," +
                    "net.sf.vanadis.core.reflection," +
                    "net.sf.vanadis.core.time," +
                    "net.sf.vanadis.core.jmx," +
                    "net.sf.vanadis.core.ver," +
                    "net.sf.vanadis.util.concurrent," +
                    "net.sf.vanadis.util.exceptions," +
                    "net.sf.vanadis.util.log," +
                    "net.sf.vanadis.util.mvn," +
                    "net.sf.vanadis.util.xml," +
                    "net.sf.vanadis.blueprints";

    public static final String JDK = "javax.accessibility," +
            "javax.activation," +
            "javax.activity," +
            "javax.annotation," +
            "javax.annotation.processing," +
            "javax.crypto," +
            "javax.crypto.interfaces," +
            "javax.crypto.spec," +
            "javax.imageio," +
            "javax.imageio.event," +
            "javax.imageio.metadata," +
            "javax.imageio.plugins.bmp," +
            "javax.imageio.plugins.jpeg," +
            "javax.imageio.spi," +
            "javax.imageio.stream," +
            "javax.jws," +
            "javax.jws.soap," +
            "javax.lang.model," +
            "javax.lang.model.element," +
            "javax.lang.model.type," +
            "javax.lang.model.util," +
            "javax.management," +
            "javax.management.loading," +
            "javax.management.modelmbean," +
            "javax.management.monitor," +
            "javax.management.openmbean," +
            "javax.management.relation," +
            "javax.management.remote," +
            "javax.management.remote.rmi," +
            "javax.management.timer," +
            "javax.naming," +
            "javax.naming.directory," +
            "javax.naming.event," +
            "javax.naming.ldap," +
            "javax.naming.spi," +
            "javax.net," +
            "javax.net.ssl," +
            "javax.print," +
            "javax.print.attribute," +
            "javax.print.attribute.standard," +
            "javax.print.event," +
            "javax.rmi," +
            "javax.rmi.CORBA," +
            "javax.rmi.ssl," +
            "javax.script," +
            "javax.security.auth," +
            "javax.security.auth.callback," +
            "javax.security.auth.kerberos," +
            "javax.security.auth.login," +
            "javax.security.auth.spi," +
            "javax.security.auth.x500," +
            "javax.security.cert," +
            "javax.security.sasl," +
            "javax.sound.midi," +
            "javax.sound.midi.spi," +
            "javax.sound.sampled," +
            "javax.sound.sampled.spi," +
            "javax.sql," +
            "javax.sql.rowset," +
            "javax.sql.rowset.serial," +
            "javax.sql.rowset.spi," +
            "javax.swing," +
            "javax.swing.border," +
            "javax.swing.colorchooser," +
            "javax.swing.event," +
            "javax.swing.filechooser," +
            "javax.swing.plaf," +
            "javax.swing.plaf.basic," +
            "javax.swing.plaf.metal," +
            "javax.swing.plaf.multi," +
            "javax.swing.plaf.synth," +
            "javax.swing.table," +
            "javax.swing.text," +
            "javax.swing.text.html," +
            "javax.swing.text.html.parser," +
            "javax.swing.text.rtf," +
            "javax.swing.tree," +
            "javax.swing.undo," +
            "javax.tools," +
            "javax.transaction," +
            "javax.transaction.xa," +
            "javax.xml;version=\"1.0.1\"," +
            "javax.xml.bind," +
            "javax.xml.bind.annotation," +
            "javax.xml.bind.annotation.adapters," +
            "javax.xml.bind.attachment," +
            "javax.xml.bind.helpers," +
            "javax.xml.bind.util," +
            "javax.xml.crypto," +
            "javax.xml.crypto.dom," +
            "javax.xml.crypto.dsig," +
            "javax.xml.crypto.dsig.dom," +
            "javax.xml.crypto.dsig.keyinfo," +
            "javax.xml.crypto.dsig.spec," +
            "javax.xml.datatype," +
            "javax.xml.namespace," +
            "javax.xml.parsers," +
            "javax.xml.soap," +
            "javax.xml.stream;version=\"1.0.1\"," +
            "javax.xml.stream.events;version=\"1.0.1\"," +
            "javax.xml.stream.util;version=\"1.0.1\"," +
            "javax.xml.transform," +
            "javax.xml.transform.dom," +
            "javax.xml.transform.sax," +
            "javax.xml.transform.stax," +
            "javax.xml.transform.stream," +
            "javax.xml.validation," +
            "javax.xml.ws," +
            "javax.xml.ws.handler," +
            "javax.xml.ws.handler.soap," +
            "javax.xml.ws.http," +
            "javax.xml.ws.soap," +
            "javax.xml.ws.spi," +
            "javax.xml.xpath," +
            "org.ietf.jgss," +
            "org.omg.CORBA," +
            "org.omg.CORBA_2_3," +
            "org.omg.CORBA_2_3.portable," +
            "org.omg.CORBA.DynAnyPackage," +
            "org.omg.CORBA.ORBPackage," +
            "org.omg.CORBA.portable," +
            "org.omg.CORBA.TypeCodePackage," +
            "org.omg.CosNaming," +
            "org.omg.CosNaming.NamingContextExtPackage," +
            "org.omg.CosNaming.NamingContextPackage," +
            "org.omg.Dynamic," +
            "org.omg.DynamicAny," +
            "org.omg.DynamicAny.DynAnyFactoryPackage," +
            "org.omg.DynamicAny.DynAnyPackage," +
            "org.omg.IOP," +
            "org.omg.IOP.CodecFactoryPackage," +
            "org.omg.IOP.CodecPackage," +
            "org.omg.Messaging," +
            "org.omg.PortableInterceptor," +
            "org.omg.PortableInterceptor.ORBInitInfoPackage," +
            "org.omg.PortableServer," +
            "org.omg.PortableServer.CurrentPackage," +
            "org.omg.PortableServer.POAManagerPackage," +
            "org.omg.PortableServer.POAPackage," +
            "org.omg.PortableServer.portable," +
            "org.omg.PortableServer.ServantLocatorPackage," +
            "org.omg.SendingContext," +
            "org.omg.stub.java.rmi," +
            "org.w3c.dom," +
            "org.w3c.dom.bootstrap," +
            "org.w3c.dom.events," +
            "org.w3c.dom.ls," +
            "org.xml.sax," +
            "org.xml.sax.ext," +
            "org.xml.sax.helpers," +
            "org.osgi.framework;version=1.3.0," +
            "org.osgi.service.packageadmin;version=1.2.0," +
            "org.osgi.service.startlevel;version=1.0.0," +
            "org.osgi.service.url;version=1.0.0," +
            "com.sun.org.apache.xalan.internal," +
            "com.sun.org.apache.xalan.internal.res," +
            "com.sun.org.apache.xml.internal.utils," +
            "com.sun.org.apache.xpath.internal," +
            "com.sun.org.apache.xpath.internal.jaxp," +
            "com.sun.org.apache.xpath.internal.objects," +
            "com.sun.xml.internal," +
            "com.sun.xml.internal.bind," +
            "com.sun.xml.internal.bind.annotation," +
            "com.sun.xml.internal.bind.api," +
            "com.sun.xml.internal.bind.api.impl," +
            "com.sun.xml.internal.bind.marshaller," +
            "com.sun.xml.internal.bind.unmarshaller," +
            "com.sun.xml.internal.bind.util," +
            "com.sun.xml.internal.bind.v2," +
            "com.sun.xml.internal.bind.v2.bytecode," +
            "com.sun.xml.internal.bind.v2.model," +
            "com.sun.xml.internal.bind.v2.model.annotation," +
            "com.sun.xml.internal.bind.v2.model.core," +
            "com.sun.xml.internal.bind.v2.model.impl," +
            "com.sun.xml.internal.bind.v2.model.nav," +
            "com.sun.xml.internal.bind.v2.model.runtime," +
            "com.sun.xml.internal.bind.v2.runtime," +
            "com.sun.xml.internal.bind.v2.runtime.output," +
            "com.sun.xml.internal.bind.v2.runtime.property," +
            "com.sun.xml.internal.bind.v2.runtime.reflect," +
            "com.sun.xml.internal.bind.v2.runtime.reflect.opt," +
            "com.sun.xml.internal.bind.v2.runtime.unmarshaller," +
            "com.sun.xml.internal.bind.v2.schemagen," +
            "com.sun.xml.internal.bind.v2.schemagen.episode," +
            "com.sun.xml.internal.bind.v2.schemagen.xmlschema," +
            "com.sun.xml.internal.bind.v2.util," +
            "com.sun.xml.internal.dtdparser," +
            "com.sun.xml.internal.dtdparser.resources," +
            "com.sun.xml.internal.fastinfoset," +
            "com.sun.xml.internal.fastinfoset.algorithm," +
            "com.sun.xml.internal.fastinfoset.alphabet," +
            "com.sun.xml.internal.fastinfoset.dom," +
            "com.sun.xml.internal.fastinfoset.org," +
            "com.sun.xml.internal.fastinfoset.org.apache," +
            "com.sun.xml.internal.fastinfoset.org.apache.xerces," +
            "com.sun.xml.internal.fastinfoset.org.apache.xerces.util," +
            "com.sun.xml.internal.fastinfoset.resources," +
            "com.sun.xml.internal.fastinfoset.sax," +
            "com.sun.xml.internal.fastinfoset.stax," +
            "com.sun.xml.internal.fastinfoset.stax.events," +
            "com.sun.xml.internal.fastinfoset.stax.factory," +
            "com.sun.xml.internal.fastinfoset.stax.util," +
            "com.sun.xml.internal.fastinfoset.tools," +
            "com.sun.xml.internal.fastinfoset.util," +
            "com.sun.xml.internal.fastinfoset.vocab," +
            "com.sun.xml.internal.messaging," +
            "com.sun.xml.internal.messaging.saaj," +
            "com.sun.xml.internal.messaging.saaj.client," +
            "com.sun.xml.internal.messaging.saaj.client.p2p," +
            "com.sun.xml.internal.messaging.saaj.packaging," +
            "com.sun.xml.internal.messaging.saaj.packaging.mime," +
            "com.sun.xml.internal.messaging.saaj.packaging.mime.internet," +
            "com.sun.xml.internal.messaging.saaj.packaging.mime.util," +
            "com.sun.xml.internal.messaging.saaj.soap," +
            "com.sun.xml.internal.messaging.saaj.soap.dynamic," +
            "com.sun.xml.internal.messaging.saaj.soap.impl," +
            "com.sun.xml.internal.messaging.saaj.soap.name," +
            "com.sun.xml.internal.messaging.saaj.soap.ver1_1," +
            "com.sun.xml.internal.messaging.saaj.soap.ver1_2," +
            "com.sun.xml.internal.messaging.saaj.util," +
            "com.sun.xml.internal.messaging.saaj.util.transform," +
            "com.sun.xml.internal.org," +
            "com.sun.xml.internal.org.jvnet," +
            "com.sun.xml.internal.org.jvnet.fastinfoset," +
            "com.sun.xml.internal.org.jvnet.fastinfoset.sax," +
            "com.sun.xml.internal.org.jvnet.fastinfoset.sax.helpers," +
            "com.sun.xml.internal.org.jvnet.fastinfoset.stax," +
            "com.sun.xml.internal.org.jvnet.staxex," +
            "com.sun.xml.internal.rngom," +
            "com.sun.xml.internal.rngom.ast," +
            "com.sun.xml.internal.rngom.ast.builder," +
            "com.sun.xml.internal.rngom.ast.om," +
            "com.sun.xml.internal.rngom.ast.util," +
            "com.sun.xml.internal.rngom.binary," +
            "com.sun.xml.internal.rngom.binary.visitor," +
            "com.sun.xml.internal.rngom.digested," +
            "com.sun.xml.internal.rngom.dt," +
            "com.sun.xml.internal.rngom.dt.builtin," +
            "com.sun.xml.internal.rngom.nc," +
            "com.sun.xml.internal.rngom.parse," +
            "com.sun.xml.internal.rngom.parse.compact," +
            "com.sun.xml.internal.rngom.parse.host," +
            "com.sun.xml.internal.rngom.parse.xml," +
            "com.sun.xml.internal.rngom.util," +
            "com.sun.xml.internal.rngom.xml," +
            "com.sun.xml.internal.rngom.xml.sax," +
            "com.sun.xml.internal.rngom.xml.util," +
            "com.sun.xml.internal.stream," +
            "com.sun.xml.internal.stream.buffer," +
            "com.sun.xml.internal.stream.buffer.sax," +
            "com.sun.xml.internal.stream.buffer.stax," +
            "com.sun.xml.internal.stream.dtd," +
            "com.sun.xml.internal.stream.dtd.nonvalidating," +
            "com.sun.xml.internal.stream.events," +
            "com.sun.xml.internal.stream.util," +
            "com.sun.xml.internal.stream.writers," +
            "com.sun.xml.internal.txw2," +
            "com.sun.xml.internal.txw2.annotation," +
            "com.sun.xml.internal.txw2.output," +
            "com.sun.xml.internal.ws," +
            "com.sun.xml.internal.ws.addressing," +
            "com.sun.xml.internal.ws.addressing.model," +
            "com.sun.xml.internal.ws.addressing.v200408," +
            "com.sun.xml.internal.ws.api," +
            "com.sun.xml.internal.ws.api.addressing," +
            "com.sun.xml.internal.ws.api.client," +
            "com.sun.xml.internal.ws.api.fastinfoset," +
            "com.sun.xml.internal.ws.api.message," +
            "com.sun.xml.internal.ws.api.message.stream," +
            "com.sun.xml.internal.ws.api.model," +
            "com.sun.xml.internal.ws.api.model.soap," +
            "com.sun.xml.internal.ws.api.model.wsdl," +
            "com.sun.xml.internal.ws.api.pipe," +
            "com.sun.xml.internal.ws.api.pipe.helper," +
            "com.sun.xml.internal.ws.api.server," +
            "com.sun.xml.internal.ws.api.streaming," +
            "com.sun.xml.internal.ws.api.wsdl," +
            "com.sun.xml.internal.ws.api.wsdl.parser," +
            "com.sun.xml.internal.ws.api.wsdl.writer," +
            "com.sun.xml.internal.ws.binding," +
            "com.sun.xml.internal.ws.client," +
            "com.sun.xml.internal.ws.client.dispatch," +
            "com.sun.xml.internal.ws.client.sei," +
            "com.sun.xml.internal.ws.developer," +
            "com.sun.xml.internal.ws.encoding," +
            "com.sun.xml.internal.ws.encoding.fastinfoset," +
            "com.sun.xml.internal.ws.encoding.soap," +
            "com.sun.xml.internal.ws.encoding.soap.streaming," +
            "com.sun.xml.internal.ws.encoding.xml," +
            "com.sun.xml.internal.ws.fault," +
            "com.sun.xml.internal.ws.handler," +
            "com.sun.xml.internal.ws.message," +
            "com.sun.xml.internal.ws.message.jaxb," +
            "com.sun.xml.internal.ws.message.saaj," +
            "com.sun.xml.internal.ws.message.source," +
            "com.sun.xml.internal.ws.message.stream," +
            "com.sun.xml.internal.ws.model," +
            "com.sun.xml.internal.ws.model.soap," +
            "com.sun.xml.internal.ws.model.wsdl," +
            "com.sun.xml.internal.ws.protocol," +
            "com.sun.xml.internal.ws.protocol.soap," +
            "com.sun.xml.internal.ws.protocol.xml," +
            "com.sun.xml.internal.ws.resources," +
            "com.sun.xml.internal.ws.server," +
            "com.sun.xml.internal.ws.server.provider," +
            "com.sun.xml.internal.ws.server.sei," +
            "com.sun.xml.internal.ws.spi," +
            "com.sun.xml.internal.ws.streaming," +
            "com.sun.xml.internal.ws.transport," +
            "com.sun.xml.internal.ws.transport.http," +
            "com.sun.xml.internal.ws.transport.http.client," +
            "com.sun.xml.internal.ws.transport.http.server," +
            "com.sun.xml.internal.ws.util," +
            "com.sun.xml.internal.ws.util.exception," +
            "com.sun.xml.internal.ws.util.localization," +
            "com.sun.xml.internal.ws.util.pipe," +
            "com.sun.xml.internal.ws.util.resources," +
            "com.sun.xml.internal.ws.util.xml," +
            "com.sun.xml.internal.ws.wsdl," +
            "com.sun.xml.internal.ws.wsdl.parser," +
            "com.sun.xml.internal.ws.wsdl.writer," +
            "com.sun.xml.internal.ws.wsdl.writer.document," +
            "com.sun.xml.internal.ws.wsdl.writer.document.http," +
            "com.sun.xml.internal.ws.wsdl.writer.document.soap," +
            "com.sun.xml.internal.ws.wsdl.writer.document.soap12," +
            "com.sun.xml.internal.ws.wsdl.writer.document.xsd," +
            "com.sun.xml.internal.xsom," +
            "com.sun.xml.internal.xsom.impl," +
            "com.sun.xml.internal.xsom.impl.parser," +
            "com.sun.xml.internal.xsom.impl.parser.state," +
            "com.sun.xml.internal.xsom.impl.scd," +
            "com.sun.xml.internal.xsom.impl.util," +
            "com.sun.xml.internal.xsom.parser," +
            "com.sun.xml.internal.xsom.util," +
            "com.sun.xml.internal.xsom.visitor," +
            "sun.reflect," +
            "sun.misc";

    public static final String LOG4J = "org.apache.log4j;version=1.2.15," +
            "org.apache.log4j.lf5;version=1.2.15," +
            "org.apache.log4j.lf5.viewer;version=1.2.15," +
            "org.apache.log4j.lf5.viewer.images;version=1.2.15," +
            "org.apache.log4j.lf5.viewer.categoryexplorer;version=1.2.15," +
            "org.apache.log4j.lf5.viewer.configure;version=1.2.15," +
            "org.apache.log4j.lf5.config;version=1.2.15," +
            "org.apache.log4j.lf5.util;version=1.2.15," +
            "org.apache.log4j.xml;version=1.2.15," +
            "org.apache.log4j.spi;version=1.2.15," +
            "org.apache.log4j.helpers;version=1.2.15," +
            "org.apache.log4j.chainsaw;version=1.2.15," +
            "org.apache.log4j.varia;version=1.2.15," +
            "org.apache.log4j.net;version=1.2.15," +
            "org.apache.log4j.jmx;version=1.2.15," +
            "org.apache.log4j.or;version=1.2.15," +
            "org.apache.log4j.or.sax;version=1.2.15," +
            "org.apache.log4j.or.jms;version=1.2.15," +
            "org.apache.log4j.config;version=1.2.15," +
            "org.apache.log4j.nt;version=1.2.15";

    public static final String OSGI = "org.osgi.framework; version=1.6.1," +
            "org.osgi.service.packageadmin; version=1.2.0," +
            "org.osgi.service.startlevel; version=1.1.0," +
            "org.osgi.service.url; version=1.0.0";
}
