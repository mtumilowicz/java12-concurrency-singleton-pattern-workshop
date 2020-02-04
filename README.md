# java12-concurrency-singleton-workshop

* https://www.amazon.com/Java-Concurrency-Practice-Brian-Goetz/dp/0321349601
* [WJUG #257 - Krzysztof Ślusarski - Just-In-Time compiler - ukryty "przyjaciel"](https://www.youtube.com/watch?v=f8zaYDJctTA) 42.25
* https://stackoverflow.com/questions/29883403/double-checked-locking-without-volatile
* http://www.cs.umd.edu/~pugh/java/memoryModel/DoubleCheckedLocking.html
* https://errorprone.info/bugpattern/DoubleCheckedLocking
* https://wiki.sei.cmu.edu/confluence/display/java/LCK10-J.+Use+a+correct+form+of+the+double-checked+locking+idiom
* https://github.com/mtumilowicz/java8-concurrency-jcstress-happens-before

# publication
* the risks of improper publication are consequences of the absence of a happens-before ordering between 
publishing a shared object and accessing it from another thread
* unfortunately, simply storing a reference to an object into a public field, is not enough to publish that 
object safely
* improper publication could allow another thread to observe a partially constructed object
    ```
    // Unsafe publication
    public Holder holder;
    
    public void initialize() {
        holder = new Holder(42);
    }
    
    // setters, getters
    public class Holder {
        private int n;
        public Holder(int n) { this.n = n; }
    
        // a thread may see a stale value (ex. default - 0) the first time it reads a field and then a 
        // more up-to-date value the next time, which is why assertSanity can throw AssertionError
        public void assertSanity() {
            if (n != n) {
                throw new AssertionError("This statement is false.");
            }
        }
    }
    ```
    * two things can go wrong
        * other threads could see a null reference
        * other threads could see an up-to-date value for the holder reference, but stale values for the 
        state of the Holder
            ```
            public void initialize() {
                var holder = new Holder(42);
                holder.setN(50);
                this.holder = holder;
            }
            ```
            could be reordered to
            ```
            public void initialize() {
                var holder = new Holder(42);
                this.holder = holder;
                holder.setN(50);
            }
            ```
## unsafe publication
* the possibility of reordering in the absence of a happens-before relationship explains why publishing an 
object without adequate synchronization can allow another thread to see a partially constructed object
* unsafe publication can happen as a result of an incorrect lazy initialization
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
    * since neither thread used synchronization, B could possibly see A’s actions in a different 
    order than A performed them 
    * so even though A initialized the Resource before setting resource to reference it, B could 
    see the write to resource as occurring before the writes to the fields of the Resource
    * with the exception of immutable objects, it is not safe to use an object that
      has been initialized by another thread unless the publication happens-before the consuming thread uses it
## safe publication
* it sometimes makes sense to defer initialization of objects that are expensive to initialize 
until they are actually needed
* the treatment of static fields with initializers (or fields whose value is initialized in a static 
initialization block is somewhat special and offers additional thread-safety guarantees. 
    * static initializers are run by the JVM at class initialization time, after class loading but 
    before the class is used by any thread
    * because the JVM acquires a lock during initialization [JLS 12.4.2] and this lock is acquired by 
    each thread at least once to ensure that the class has been loaded, memory writes made during static 
    initialization are automatically visible to all threads
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
* ResourceHolder
    ```
    @ThreadSafe
    public class LazySingleton {
        private static class ResourceHolder {
            public static Resource resource = new Resource();
        }
        public static Resource getResource() {
            return ResourceHolder.resource;
        }
    }
    ```
    * the JVM defers initializing the ResourceHolder class until it is actually used [JLS 12.4.1], and because the
      Resource is initialized with a static initializer, no additional synchronization is
      needed
    * the first call to getResource by any thread causes ResourceHolder to be
      loaded and initialized, at which time the initialization of the Resource happens
      through the static initializer
## immutability
## safe publication
* objects that are not immutable must be safely published, which usually entails synchronization by both the 
publishing and the consuming thread
* to publish an object safely, both the reference to the object and the object’s state must be made visible 
to other threads at the same time. 
* a properly constructed object can be safely published by:
    * initializing an object reference from a static initializer
    * storing a reference to it into a volatile field or AtomicReference
    * storing a reference to it into a final field of a properly constructed object
    * storing a reference to it into a field that is properly guarded by a lock
* static initializers are executed by the JVM at class initialization time; because
  of internal synchronization in the JVM, this mechanism is guaranteed to safely
  publish any objects initialized in this way [JLS 12.4.2]        

# double-checked locking
```
@NotThreadSafe
class DoubleCheckedLockingSingleton {
    private static Resource resource;

    public static Resource getInstance() {
        if (resource == null) {
            synchronized (DoubleCheckedLockingSingleton.class) {
                if (resource == null)
                    resource = new Resource();
            }
        }
        return resource;
    }
}
```
* the real problem with DCL is the assumption that the worst thing that can
    happen when reading a shared object reference without synchronization is to
    erroneously see a stale value (in this case, null )
* in that case the DCL idiom compensates for this risk by trying again with the lock held
* but the worst case - it is possible to see a current value of the reference but stale values 
for the object’s state, meaning that the object could be seen to be in an invalid or incorrect state
* the lazy initialization holder idiom offers the same benefits and is easier to understand

# initialization safety
* without initialization safety, immutable objects like String can change their value (in case of no synchronization)
* security architecture relies on the immutability of String
    * lack of initialization safety could create security vulnerabilities
* initialization safety guarantees that for properly constructed objects, all
  threads will see the correct values of final fields that were set by the constructor, 
  regardless of how the object is published.
  * any variables that can be reached through a final field of a properly constructed object
  (such as the elements of a final array or the contents of a HashMap referenced by a final field) 
  are also guaranteed to be visible to other threads
* for objects with final fields, initialization safety prohibits reordering any part
  of construction with the initial load of a reference to that object
* all writes to final fields made by the constructor, as well as to any variables reachable through those
  fields, become "frozen" when the constructor completes, and any thread that
  obtains a reference to that object is guaranteed to see a value that is at least as up
  to date as the frozen value
    * writes that initialize variables reachable through final fields are not reordered with 
    operations following the post-construction freeze
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
* a number of small changes to SafeStates would take away its thread safety
    * if states were not final, or if any method other than the constructor modified its contents, 
    initialization safety would not be strong enough to safely access SafeStates without synchronization
    * if SafeStates had other nonfinal fields, other threads might still see incorrect values of those fields
    * allowing the object to escape during construction invalidates the initialization-safety
    guarantee