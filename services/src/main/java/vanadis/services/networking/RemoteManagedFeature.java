package vanadis.services.networking;

import vanadis.common.io.Location;

import java.io.Serializable;

public interface RemoteManagedFeature<T extends RemoteManagedFeature<T>> extends Serializable {

    Location getRemoteLocation();

    String getServiceInterfaceName();

    T copyRelocatedAt(Location location);

    int getPid();
}
