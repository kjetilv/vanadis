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
package vanadis.modules.dist;

import vanadis.ext.*;
import vanadis.services.remoting.Remoting;
import vanadis.util.collect.Generic;
import vanadis.util.io.Location;
import vanadis.util.lang.ToString;
import vanadis.util.log.Log;
import vanadis.util.log.Logs;
import vanadis.util.time.TimeSpan;
import org.jgroups.JChannel;
import org.w3c.dom.Element;

import java.util.List;

@Module(moduleType = "distributor")
@SuppressWarnings({"UnusedDeclaration", "FieldCanBeLocal"})
public class DistributorModule extends AbstractModule {

    private static final Log log = Logs.get(DistributorModule.class);

    private static final String DISTRIBUTED = "distributed";

    private final List<Object> distributeds = Generic.list();

    private Location endPoint;

    @Configure(required = false)
    private Element jGroupsConfiguration;

    @Configure(def = "30s")
    private TimeSpan advertiseInterval;

    private LocationAdvertiser advertiser;

    private Gossiper gossiper;

    private String locationGroup;

    private String registrationsGroup;

    @Inject(attributeName = DISTRIBUTED, required = false, remotable = true)
    public void addDistributed(Object distributed) {
        log.info(this + " notified of added: " + distributed);
        distributeds.add(distributed);
    }

    @Retract(attributeName = DISTRIBUTED)
    public void removeDistributed(Object distributed) {
        log.info(this + " notified of removed: " + distributed);
        distributeds.remove(distributed);
    }

    @Inject(required = true, retained = false)
    public void setRemoting(Remoting remoting) {
        setEndPoint(remoting == null ? null : remoting.getEndPoint());
    }

    public DistributorModule setEndPoint(Location endPoint) {
        this.endPoint = endPoint;
        return this;
    }

    @Configure(def = "${vanadis.host}")
    public static void setBindAddress(String host) {
        System.setProperty("jgroups.bind_addr", host);
    }

    @Configure(def = "vanadis")
    public void setGroup(String group) {
        this.locationGroup = group + "-location";
        this.registrationsGroup = group + "-registrations";
    }

    @SuppressWarnings({"RedundantMethodOverride"})
    @Override
    public void activate() {
//        ScheduledExecutorService service = Executors.newScheduledThreadPool(2);
//        advertiser = new LocationAdvertiser
//            (service, createChannel(locationGroup), advertiseInterval, endPoint);
//        gossiper = new Gossiper
//            (service, createChannel(registrationsGroup), endPoint);
    }

    private JChannel createChannel(String group) {
        JChannel channel;
        try {
            channel = jGroupsConfiguration == null ? new JChannel() : new JChannel(jGroupsConfiguration);
        } catch (Exception e) {
            throw new IllegalStateException(" Failed to create channel", e);
        }
        try {
            channel.setReceiver(new MessageReceiver());
        } catch (Throwable e) {
            throw new IllegalStateException("Failed to configure channel", e);
        }
        try {
            channel.connect(group);
        } catch (Throwable e) {
            throw new IllegalStateException("Failed to open channel " + channel, e);
        }
        return channel;
    }

    @Override
    public String toString() {
        return ToString.of(this, endPoint, "advertiser", advertiser);
    }

}
