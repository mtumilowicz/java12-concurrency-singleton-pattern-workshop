package answers;

import resource.Resource;

class EagerSingletonAnswer {

    private static Resource resource = new Resource();

    static Resource getInstance() {
        return resource;
    }
}
