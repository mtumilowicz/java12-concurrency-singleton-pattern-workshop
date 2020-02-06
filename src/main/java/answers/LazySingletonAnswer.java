package answers;

import resource.Resource;

class LazySingletonAnswer {
    private static class ResourceHolder {
        static Resource resource = new Resource();
    }

    static Resource getInstance() {
        return ResourceHolder.resource;
    }
}
