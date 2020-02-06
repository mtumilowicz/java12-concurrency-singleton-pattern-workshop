package answers

import resource.Resource
import spock.lang.Specification

import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

class LazySingletonAnswerTest extends Specification {

    def singleton = LazySingletonAnswer

    def 'multithreading GetInstance'() {
        given:
        def concurrentList = new ConcurrentLinkedDeque<Resource>()

        and: 'latch to signalize end'
        CountDownLatch latch = new CountDownLatch(2)

        and: 'task to add singleton instance to list'
        Runnable task = {
            concurrentList.push(singleton.instance)
            latch.countDown()
        }

        and: 'create executor service'
        def es = Executors.newCachedThreadPool()

        when: 'run concurrently'
        es.submit(task)
        es.submit(task)

        and: 'wait for all tasks to end'
        latch.await()

        and: 'shutdown executor service'
        es.shutdownNow()

        then: 'same instance in list'
        concurrentList.size() == 2
        concurrentList.pop() is concurrentList.pop()
    }

}
