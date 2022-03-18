package uz.app.Anno.orm;

public class RepositoryFactory {
    public Repository getRepository(Class<? extends BaseEntity> entityClass) throws Exception
    {
        Repository repo = new Repository<BaseEntity>();
        repo.SetTargetEntity(entityClass);
        return repo;
    }
}
