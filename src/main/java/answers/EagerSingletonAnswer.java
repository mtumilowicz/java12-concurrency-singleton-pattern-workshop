package answers;

import resource.Resource;

class EagerSingletonAnswer {
    private static Resource resource = new Resource();

    public static Resource getResource() {
        return resource;
    }
}
