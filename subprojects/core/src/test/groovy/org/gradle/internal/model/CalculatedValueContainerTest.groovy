/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.internal.model

import org.gradle.api.Project
import org.gradle.api.internal.tasks.NodeExecutionContext
import org.gradle.api.internal.tasks.TaskDependencyResolveContext
import org.gradle.internal.Describables
import org.gradle.test.fixtures.concurrent.ConcurrentSpec

import java.util.concurrent.atomic.AtomicInteger

class CalculatedValueContainerTest extends ConcurrentSpec {
    def "can create container with fixed value"() {
        def container = CalculatedValueContainer.of(Describables.of("thing"), "value")

        expect:
        container.get() == "value"
        container.getValue().get() == "value"
    }

    def "calculates and caches value"() {
        def calculator = Mock(ValueCalculator)

        when:
        def container = CalculatedValueContainer.of(Describables.of("<thing>"), calculator)

        then:
        0 * _

        when:
        container.run(Stub(NodeExecutionContext))

        then:
        1 * calculator.calculateValue(_) >> "result"
        0 * _

        when:
        def result = container.get()
        def result2 = container.getValue().get()

        then:
        result == "result"
        result2 == "result"

        and:
        0 * _
    }

    def "retains and rethrows failure to calculate value"() {
        def failure = new RuntimeException()
        def calculator = Mock(ValueCalculator)

        when:
        def container = CalculatedValueContainer.of(Describables.of("<thing>"), calculator)

        then:
        0 * _

        when:
        // NOTE: does not rethrow exception here
        container.run(Stub(NodeExecutionContext))

        then:
        1 * calculator.calculateValue(_) >> { throw failure }
        0 * _

        when:
        def result = container.getValue()

        then:
        result.failure.get() == failure

        and:
        0 * _

        when:
        container.get()

        then:
        def e = thrown(RuntimeException)
        e == failure

        and:
        0 * _
    }

    def "cannot get value before it has been calculated"() {
        def calculator = Mock(ValueCalculator)
        def container = CalculatedValueContainer.of(Describables.of("<thing>"), calculator)

        when:
        container.get()

        then:
        def e = thrown(IllegalStateException)
        e.message == 'Value for <thing> has not been calculated yet.'

        when:
        container.getValue().get()

        then:
        def e2 = thrown(IllegalStateException)
        e2.message == 'Value for <thing> has not been calculated yet.'
    }

    def "at most one thread calculates the value"() {
        // Don't use a spock mock as these apply their own synchronization
        def container = CalculatedValueContainer.of(Describables.of("<thing>"), new Calculator())

        when:
        async {
            10.times {
                start {
                    container.calculateIfNotAlready(null)
                    assert container.get() == 1
                }
            }
        }

        then:
        container.get() == 1
    }

    static class Calculator implements ValueCalculator<Integer> {
        private final AtomicInteger value = new AtomicInteger()

        @Override
        Integer calculateValue(NodeExecutionContext context) {
            def changed = value.compareAndSet(0, 1)
            assert changed
            return 1
        }

        @Override
        boolean usesMutableProjectState() {
            return false
        }

        @Override
        Project getOwningProject() {
            return null
        }

        @Override
        void visitDependencies(TaskDependencyResolveContext context) {
        }
    }
}
