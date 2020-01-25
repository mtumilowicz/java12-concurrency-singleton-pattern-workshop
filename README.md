# java12-concurrency-singleton-workshop

* https://www.amazon.com/Java-Concurrency-Practice-Brian-Goetz/dp/0321349601 344
* [WJUG #257 - Krzysztof Ślusarski - Just-In-Time compiler - ukryty "przyjaciel"](https://www.youtube.com/watch?v=f8zaYDJctTA) 42.25
* https://stackoverflow.com/questions/29883403/double-checked-locking-without-volatile
* http://www.cs.umd.edu/~pugh/java/memoryModel/DoubleCheckedLocking.html
* https://errorprone.info/bugpattern/DoubleCheckedLocking
* https://wiki.sei.cmu.edu/confluence/display/java/LCK10-J.+Use+a+correct+form+of+the+double-checked+locking+idiom
* https://github.com/mtumilowicz/java8-concurrency-jcstress-happens-before

* 3.5 Safe publication
    * Unfortunately, simply storing a reference to an object into a public
      field, is not enough to publish that object safely
    * This improper publication could allow another thread to observe
      a partially constructed object
    * 3.5.1 Improper publication: when good objects go bad
        ```
        // Unsafe publication
        public Holder holder;
        
        public void initialize() {
            holder = new Holder(42);
        }
        
        public class Holder {
            private int n;
            public Holder(int n) { this.n = n; }
            
            public void assertSanity() {
                if (n != n)
                    throw new AssertionError("This statement is false.");
            }
        }
        
        ```
        * Two things can go wrong
          with improperly published objects. Other threads could see a stale value for the
          holder field, and thus see a null reference or other older value even though a
          value has been placed in holder . But far worse, other threads could see an up-to-
          date value for the holder reference, but stale values for the state of the Holder
        * a thread may see a stale value the first time
          it reads a field and then a more up-to-date value the next time, which is why
          assertSanity can throw AssertionError
    * 3.5.2 Immutable objects and initialization safety
        * the Java Memory Model offers a spe-
          cial guarantee of initialization safety for sharing immutable objects
        *  can be safely accessed even when synchro-
          nization is not used to publish the object reference
        * However, if final fields refer to mutable objects, synchronization is still required
          to access the state of the objects they refer to
    * 3.5.3 Safe publication idioms
        * Objects that are not immutable must be safely published, which usually entails syn-
          chronization by both the publishing and the consuming thread
        * To publish an object safely, both the reference to the object and the ob-
          ject’s state must be made visible to other threads at the same time. A
          properly constructed object can be safely published by:
          * Initializing an object reference from a static initializer;
          * Storing a reference to it into a volatile field or AtomicReference;
          * Storing a reference to it into a final field of a properly constructed
          object; or
          * Storing a reference to it into a field that is properly guarded by a
          lock.
        * Static initializers are executed by the JVM at class initialization time; because
          of internal synchronization in the JVM, this mechanism is guaranteed to safely
          publish any objects initialized in this way [JLS 12.4.2]
        

* 16.2 Publication
    * the risks of improper publication are consequences of the
      absence of a happens-before ordering between publishing a shared object and ac-
      cessing it from another thread
    * 16.2.1 Unsafe publication
        * The possibility of reordering in the absence of a happens-before relationship ex-
          plains why publishing an object without adequate synchronization can allow an-
          other thread to see a partially constructed object
        * Initializing a new
          object involves writing to variables—the new object’s fields. Similarly, publishing
          a reference involves writing to another variable—the reference to the new object
        * If you do not ensure that publishing the shared reference happens-before another
          thread loads that shared reference, then the write of the reference to the new ob-
          ject can be reordered (from the perspective of the thread consuming the object)
          with the writes to its fields
        * In that case, another thread could see an up-to-date
          value for the object reference but out-of-date values for some or all of that object’s
          state—a partially constructed object
        * Unsafe publication can happen as a result of an incorrect lazy initialization
            ```
            @NotThreadSafe
            public class UnsafeLazyInitialization {
                private static Resource resource;
                public static Resource getInstance() {
                    if (resource == null) {
                        resource = new Resource(); // unsafe publication
                    }
                    return resource;
                }
            }
            ```
            * Suppose thread A is the first to invoke getInstance . It sees that resource is
              null , instantiates a new Resource , and sets resource to reference it. When thread
              B later calls getInstance , it might see that resource already has a non-null value
              and just use the already constructed Resource
            * Since neither thread used synchronization, B could possibly see A’s ac-
              tions in a different order than A performed them. So even though A initialized
              the Resource before setting resource to reference it, B could see the write to
              resource as occurring before the writes to the fields of the Resource
            * B could thus
              see a partially constructed Resource that may well be in an invalid state—and
              whose state may unexpectedly change later.
            * With the exception of immutable objects, it is not safe to use an object that
              has been initialized by another thread unless the publication happens-
              before the consuming thread uses it
    * 16.2.2 Safe publication
        * https://github.com/mtumilowicz/java-this-escaping-constructor