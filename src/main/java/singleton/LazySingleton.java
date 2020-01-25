package singleton;

import resource.Resource;

public class LazySingleton {
    private static class ResourceHolder {
        public static Resource resource = new Resource();
    }
    public static Resource getResource() {
        return ResourceHolder.resource;
    }
}
