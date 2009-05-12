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
package vanadis.core.lang;

import vanadis.core.collections.Generic;

import java.util.List;

final class Sieve {

    static List<Integer> oddPrimes(int x) {
        List<Integer> primes = oddNumbersLowerThan(x);
        removeProducts(primes);
        return primes;
    }

    private static void removeProducts(List<Integer> primes) {
        for (int i : copyOf(primes)) {
            List<Integer> products = productsOfInt(i, primes);
            primes.removeAll(products);
        }
    }

    private static List<Integer> productsOfInt(int i, List<Integer> primes) {
        List<Integer> products = Generic.list();
        for (Integer odd : primes) {
            int product = odd * i;
            if (primes.contains(product)) {
                products.add(product);
            }
        }
        return products;
    }

    private static List<Integer> copyOf(List<Integer> primes) {
        return Generic.list(primes);
    }

    private static List<Integer> oddNumbersLowerThan(int x) {
        List<Integer> mainList = Generic.list();
        // Init main arrayList with odd numbers
        for (int i = 3; i < x; i += 2) {
            mainList.add(i);
        }
        return mainList;
    }

    private Sieve() { }
}
