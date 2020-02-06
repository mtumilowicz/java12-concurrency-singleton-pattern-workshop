package workshop;

import resource.Resource;

class DoubleCheckedLockingSingletonWorkshop {

    static Resource getInstance() {
        return new Resource();
    }
}
