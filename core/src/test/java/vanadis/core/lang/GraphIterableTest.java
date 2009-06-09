package vanadis.core.lang;

import junit.framework.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings({"StandardVariableNames"})
public class GraphIterableTest {

    static class N implements Iterable<N> {

        private final List<N> ns;

        private final String name;

        @Override
        public String toString() {
            return name;
        }

        N(String name, N... ns) {
            this.name = name;
            this.ns = Arrays.asList(ns);
        }

        @Override
        public Iterator<N> iterator() {
            return ns.iterator();
        }
    }

    @Test
    public void simpleGraph() {
        N root = new N("root");
        N c1 = new N("c1", root);
        N c2 = new N("c2", root);
        N bottom = new N("bottom", c1, c2);
        assertN(bottom, bottom, c1, root, c2);
    }

    @Test
    public void moreComplexGraph() {
        N n = new N("root");
        N s1 = new N("s1", n);
        N s2 = new N("s2", n);
        N s3 = new N("s3", n);

        N ss231 = new N("ss231", s2, s3);
        N ss232 = new N("ss231", s2, s3);

        N bottom = new N("bottom", s1, ss231, ss232);

        assertN(bottom, bottom, s1, n, ss231, s2, s3, ss232);
    }

    private static GraphIterable<N> iterable(final N bottom) {
        return new GraphIterable<N>(bottom) {
            @Override
            protected Iterable<N> iterable(N n) {
                return n;
            }
        };
    }

    private static void assertN(N nn, N... ns) {
        GraphIterable<N> iterable = iterable(nn);
        Iterator<N> it = iterable.iterator();
        for (N n : ns) {
            Assert.assertEquals(n, it.next());
        }
        Assert.assertFalse(it.hasNext());
    }
}
