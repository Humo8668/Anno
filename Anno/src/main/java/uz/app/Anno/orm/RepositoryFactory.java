package uz.app.Anno.orm;

public class RepositoryFactory {
    public Repository getRepository(Class entityClass)
    {
        Repository repo = new Repository<>();
        return repo;
    }
}
