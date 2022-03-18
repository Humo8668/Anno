package uz.app.Anno.test;

import uz.app.Anno.orm.Repository;

public class UserRepository extends Repository<User> {
    public UserRepository() throws Exception{
        Class.forName("org.postgresql.Driver");
        SetTargetEntity(User.class);
    }
}
