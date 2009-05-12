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
package vanadis.modules.examples.javacalc.calcservices;

import vanadis.ext.Expose;
import vanadis.ext.Track;

import java.util.Set;

public interface OperatorWiring {

    @Track(trackedType = Adder.class, remotable = true)
    Set<Adder> getAdders();

    @Track(trackedType = Subtractor.class, remotable = true)
    Set<Subtractor> getSubtractors();

    @Track(trackedType = Multiplier.class, remotable = true)
    Set<Multiplier> getMultipliers();

    @Track(trackedType = Divisor.class, remotable = true)
    Set<Divisor> getDivisors();

    @Expose
    PocketCalculator getPocketCalculator();
}
