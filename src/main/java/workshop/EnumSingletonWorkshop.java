package workshop;

import resource.Resource;

enum EnumSingletonWorkshop {
    INSTANCE;

    static Resource getInstance() {
        return new Resource();
    }
}
