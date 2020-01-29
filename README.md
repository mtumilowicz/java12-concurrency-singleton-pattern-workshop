# java12-concurrency-singleton-workshop

* https://www.amazon.com/Java-Concurrency-Practice-Brian-Goetz/dp/0321349601
* [WJUG #257 - Krzysztof Ślusarski - Just-In-Time compiler - ukryty "przyjaciel"](https://www.youtube.com/watch?v=f8zaYDJctTA) 42.25
* https://stackoverflow.com/questions/29883403/double-checked-locking-without-volatile
* http://www.cs.umd.edu/~pugh/java/memoryModel/DoubleCheckedLocking.html
* https://errorprone.info/bugpattern/DoubleCheckedLocking
* https://wiki.sei.cmu.edu/confluence/display/java/LCK10-J.+Use+a+correct+form+of+the+double-checked+locking+idiom
* https://github.com/mtumilowicz/java8-concurrency-jcstress-happens-before

# publication
* the risks of improper publication are consequences of the
  absence of a happens-before ordering between publishing a shared object and accessing it from another thread
* Unfortunately, simply storing a reference to an object into a public
  field, is not enough to publish that object safely
* This improper publication could allow another thread to observe
  a partially constructed object
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
* Two things can go wrong with improperly published objects. 
    * other threads could see a stale value for the holder field
        * see a null reference or other older value  even though a value has been placed in holder
    * other threads could see an up-to-date value for the holder reference, but stale values for the 
    state of the Holder
        * a thread may see a stale value the first time it reads a field and then a more up-to-date value the next time, 
        which is why assertSanity can throw AssertionError
## unsafe publication
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
## safe publication
* Safe initialization idioms
* it sometimes makes sense to defer initialization of objects that are expensive to
   initialize until they are actually needed
* The treatment of static fields with initializers (or fields whose value is initial-
  ized in a static initialization block [JPL 2.2.1 and 2.5.3]) is somewhat special and
* offers additional thread-safety guarantees. Static initializers are run by the JVM
  at class initialization time, after class loading but before the class is used by any
  thread. Because the JVM acquires a lock during initialization [JLS 12.4.2] and this
  lock is acquired by each thread at least once to ensure that the class has been
  loaded, memory writes made during static initialization are automatically visible
  to all threads
    ```
    @ThreadSafe
    public class SafeLazyInitialization {
        private static Resource resource;
        public synchronized static Resource getInstance() { // do we need this synchronization?
            if (resource == null)
                resource = new Resource();
            return resource;
        }
    }
    ```
* ReousrceHolder
    * The JVM defers initializ-
      ing the ResourceHolder class until it is actually used [JLS 12.4.1], and because the
      Resource is initialized with a static initializer, no additional synchronization is
      needed.
    * The first call to getResource by any thread causes ResourceHolder to be
      loaded and initialized, at which time the initialization of the Resource happens
      through the static initializer.
## immutability
## safe publication
* Objects that are not immutable must be safely published, which usually entails synchronization by both the 
publishing and the consuming thread
* To publish an object safely, both the reference to the object and the object’s state must be made visible 
to other threads at the same time. 
* A properly constructed object can be safely published by:
  * Initializing an object reference from a static initializer;
  * Storing a reference to it into a volatile field or AtomicReference;
  * Storing a reference to it into a final field of a properly constructed object
  * Storing a reference to it into a field that is properly guarded by a lock
* Static initializers are executed by the JVM at class initialization time; because
  of internal synchronization in the JVM, this mechanism is guaranteed to safely
  publish any objects initialized in this way [JLS 12.4.2]        

# Double-checked locking
* The real problem with DCL is the assumption that the worst thing that can
  happen when reading a shared object reference without synchronization is to
  erroneously see a stale value (in this case, null ); in that case the DCL idiom
  compensates for this risk by trying again with the lock held. 
* But the worst case is
    actually considerably worse—it is possible to see a current value of the reference
    but stale values for the object’s state, meaning that the object could be seen to be
    in an invalid or incorrect state.
* The lazy initialization holder
  idiom offers the same benefits and is easier to understand.

# Initialization safety
* without initialization safety, immutable objects like String can change their value (in case of no synchronization)
* security architecture relies on the immutability of String
* lack of initialization safety could create security vulnerabilities
* initialization safety guarantees that for properly constructed objects, all
  threads will see the correct values of final fields that were set by the constructor, 
  regardless of how the object is published.
  * any variables that can be reached through a final field of a properly constructed object
  (such as the elements of a final array or the contents of a HashMap refer-
  enced by a final field) are also guaranteed to be visible to other threads. 
* for objects with final fields, initialization safety prohibits reordering any part
  of construction with the initial load of a reference to that object. 
* all writes to final fields made by the constructor, as well as to any variables reachable through those
  fields, become "frozen" when the constructor completes, and any thread that
  obtains a reference to that object is guaranteed to see a value that is at least as up
  to date as the frozen value. Writes that initialize variables reachable through final
  fields are not reordered with operations following the post-construction freeze
```
@ThreadSafe
public class SafeStates {
    private final Map<String, String> states;
    public SafeStates() {
        states = new HashMap<String, String>();
        states.put("alaska", "AK");
        states.put("alabama", "AL");
        ...
        states.put("wyoming", "WY");
    }
    public String getAbbreviation(String s) {
        return states.get(s);
    }
}
```
* However, a number of small changes to SafeStates would take away its
  thread safety. If states were not final, or if any method other than the constructor
  modified its contents, initialization safety would not be strong enough to safely
  access SafeStates without synchronization. If SafeStates had other nonfinal
  fields, other threads might still see incorrect values of those fields. And allow-
  ing the object to escape during construction invalidates the initialization-safety
  guarantee.
* Initialization safety makes visibility guarantees only for the values that
  are reachable through final fields as of the time the constructor finishes.
  For values reachable through nonfinal fields, or values that may change
  after construction, you must use synchronization to ensure visibility