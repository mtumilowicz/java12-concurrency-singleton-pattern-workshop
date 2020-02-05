package answers;

import resource.Resource;

enum EnumSingletonAnswer {
    INSTANCE;

    public final Resource resource = new Resource();
}
