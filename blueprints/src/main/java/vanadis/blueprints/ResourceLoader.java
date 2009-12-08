package vanadis.blueprints;

import java.net.URL;

public interface ResourceLoader {

    URL get(String res);
}
