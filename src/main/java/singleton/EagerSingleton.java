package singleton;

import resource.Resource;

class EagerSingleton {
    private static Resource resource = new Resource();

    public static Resource getResource() {
        return resource;
    }
}
