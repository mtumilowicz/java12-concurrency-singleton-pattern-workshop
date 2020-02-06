package workshop;

import resource.Resource;

class EagerSingletonWorkshop {

    static Resource getInstance() {
        return new Resource();
    }
}
