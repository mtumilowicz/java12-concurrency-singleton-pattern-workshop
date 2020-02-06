package answers;

import resource.Resource;

enum EnumSingletonAnswer {
    INSTANCE;

    private final Resource resource = new Resource();

    Resource get() {
        return resource;
    }
}
