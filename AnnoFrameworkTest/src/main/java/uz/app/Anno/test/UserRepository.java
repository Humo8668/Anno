package uz.app.Anno.test;

import uz.app.Anno.orm.Repository;

public class UserRepository extends Repository<User> {
    public UserRepository() throws Exception{
        SetTargetEntity(User.class);
    }
}
