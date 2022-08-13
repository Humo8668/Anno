package uz.app.Anno.orm;

import java.sql.SQLException;

import uz.app.Anno.orm.exceptions.AnnoException;

public class RepositoryFactory {

    static OrmContext context = null;

    public static void setContext(OrmContext context) {
        RepositoryFactory.context = context;
    } 

    public static OrmContext getContext() {
        return RepositoryFactory.context;
    }

    public static <T extends IEntity> Repository<T> getRepository(Class<T> entityClass) throws SQLException, AnnoException
    {
        if(RepositoryFactory.context == null) 
            throw new RuntimeException("ORM Context was not set before instantiating repository.");
        Repository<T> repo = new Repository<T>(RepositoryFactory.context);
        repo.SetTargetEntity(entityClass);
        return repo;
    }
}
