package singleton;

import resource.Resource;

public enum EnumSingleton {
    INSTANCE;

    public final Resource resource = new Resource();
}
