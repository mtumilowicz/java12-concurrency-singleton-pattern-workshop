package singleton;

import resource.Resource;

enum EnumSingleton {
    INSTANCE;

    public final Resource resource = new Resource();
}
