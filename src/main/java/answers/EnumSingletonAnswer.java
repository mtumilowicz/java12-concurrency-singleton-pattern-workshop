package answers;

import resource.Resource;

enum EnumSingletonAnswer {
    INSTANCE;

    private final Resource resource = new Resource();

    static Resource getInstance() {
        return INSTANCE.resource;
    }
}
