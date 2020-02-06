package answers;

import resource.Resource;

enum EnumSingletonAnswer {
    INSTANCE;

    final Resource resource = new Resource();
}
