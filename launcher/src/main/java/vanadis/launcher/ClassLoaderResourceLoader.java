package vanadis.launcher;

import vanadis.blueprints.ResourceLoader;

import java.net.URL;

public class ClassLoaderResourceLoader implements ResourceLoader {

    private final ClassLoader classLoader;

    public ClassLoaderResourceLoader() {
        this(Thread.currentThread().getContextClassLoader());
    }

    public ClassLoaderResourceLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public URL get(String res) {
        return classLoader.getResource(res);
    }
}
