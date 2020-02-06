package workshop;

import resource.Resource;

class LazySingletonWorkshop {
    static Resource getInstance() {
        return new Resource();
    }
}
