package uz.app.Anno.orm;

public class RepositoryFactory {
    public Repository getRepository(Class<? extends IEntity> entityClass) throws Exception
    {
        Repository repo = new Repository<IEntity>();
        repo.SetTargetEntity(entityClass);
        return repo;
    }
}
