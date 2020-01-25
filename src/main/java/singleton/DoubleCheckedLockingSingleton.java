package singleton;

import resource.Resource;

public class DoubleCheckedLockingSingleton {
    private static volatile Resource resource;

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
