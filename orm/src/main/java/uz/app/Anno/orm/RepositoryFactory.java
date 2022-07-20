package uz.app.Anno.orm;

import java.sql.SQLException;

import uz.app.Anno.orm.exceptions.AnnoException;

public class RepositoryFactory {

    OrmContext context;

    public RepositoryFactory(OrmContext context) {
        this.context = context;
    } 

    public <T extends IEntity> Repository<T> getRepository(Class<T> entityClass) throws SQLException, AnnoException
    {
        Repository<T> repo = new Repository<T>(this.context);
        repo.SetTargetEntity(entityClass);
        return repo;
    }
}
