package answers;

import resource.Resource;

class DoubleCheckedLockingSingletonAnswer {

    private static volatile Resource resource;

    static Resource getInstance() {
        if (resource == null) {
            synchronized (DoubleCheckedLockingSingletonAnswer.class) {
                if (resource == null)
                    resource = new Resource();
            }
        }
        return resource;
    }
}
