package vanadis.modules.remoting;

import vanadis.common.io.Location;
import vanadis.core.lang.ToString;

final class RemotingMBeanImpl implements RemotingMBean {

    private final RemotingModule remotingModule;

    RemotingMBeanImpl(RemotingModule remotingModule) {
        this.remotingModule = remotingModule;
    }

    @Override
    public String toString() {
        return ToString.of(this, remotingModule);
    }

    @Override
    public String getEndPoint() {
        Location location = remotingModule.getLocation();
        return location == null ? null : location.toLocationString();
    }
}
