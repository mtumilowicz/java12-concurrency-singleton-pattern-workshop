package answers;

import resource.Resource;

class LazySingletonAnswer {
    private static class ResourceHolder {
        static Resource resource = new Resource();
    }

    public static Resource getInstance() {
        return ResourceHolder.resource;
    }
}
