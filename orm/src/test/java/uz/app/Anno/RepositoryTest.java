package uz.app.Anno;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.Before;
import org.junit.Test;

import uz.app.Anno.orm.OrmContext;
import uz.app.Anno.orm.Repository;
import uz.app.Anno.orm.RepositoryFactory;
import uz.app.Anno.orm.exceptions.AnnoException;
import uz.app.Anno.orm.exceptions.ValidationException;

public class RepositoryTest {

    OrmContext context;
    Repository<User> userRepo;
    final Integer USER_HUMO_ID = 1;

    @Before
    public void setUp() throws FileNotFoundException, IOException, SQLException, AnnoException {
        context = new OrmContext("test.application.context");
        RepositoryFactory.setContext(context);
        userRepo = RepositoryFactory.getRepository(User.class);
    }

    @Test
    
    public void getAll() throws SQLException, AnnoException{
        User[] users =  userRepo.getAll();
        assertTrue(users.length > 0);
    }

    @Test
    public void getById() throws SQLException, AnnoException {
        User user =  userRepo.getById(1);
        assertTrue(user != null);
    }

    @Test
    public void save() throws SQLException, ValidationException, AnnoException {
        User newUser = new User();
        newUser.setFullName("AAA BBB");
        newUser.setLogin("login_qwerty");
        newUser.setState("A");
        newUser.setPasswordHash("asdsdsaddadas");
        newUser.setEmail("mail@mail.com");

        userRepo.save(newUser); 

        assertTrue("Id of newly saved user was not set", newUser.getId() > 0);

        User checkUser = userRepo.getById(newUser.getId());
        assertTrue("Error on saving user. Expected login: " + newUser.getLogin() + "; Got: " + checkUser.getLogin(), 
            checkUser.getLogin().trim().equals(newUser.getLogin().trim()));
        
        //createdUserId = checkUser.getId();
    }
    
    @Test
    public void update() throws SQLException, ValidationException, AnnoException  {
        User user = userRepo.getById(USER_HUMO_ID);
        if(user.getEmail().trim().endsWith("@gmail.com")) {
            user.setEmail(user.getEmail().replaceFirst("@gmail.com", "@mail.ru").trim());
        } else {
            user.setEmail("humo@gmail.com");
        }
        String newEmail = user.getEmail();
        
        userRepo.update(user);
        //-------
        user = userRepo.getById(USER_HUMO_ID);
        assertTrue("Expected: " + newEmail + "; Got: " + user.getEmail(), newEmail.trim().equals(user.getEmail().trim()));
    }

    @Test
    public void delete() throws SQLException, AnnoException {
        Connection conn = context.getPoolConnection().getConnection();
        PreparedStatement statement = conn.prepareStatement("insert into \"public\".\"Users\" (login, \"fullName\", email, \"passwordHash\", state) VALUES('user_for_deletion', 'DELETE ME', 'delete@delete@mail.com', 'deleteme', 'A')", Statement.RETURN_GENERATED_KEYS);
        statement.executeUpdate();
        ResultSet rs = statement.getGeneratedKeys();
        int userForDeletion_id = -1;
        if(rs.next()) {
            userForDeletion_id = rs.getInt("id");
        } else {
            throw new RuntimeException("No generated keys returned!");
        }
        
        //-----------------------------------------------

        User user = userRepo.getById(userForDeletion_id);
        assertTrue("Couldn't get user", user != null);
        userRepo.delete(user.getId());
        user = userRepo.getById(userForDeletion_id);
        assertTrue("User was not deleted!", user == null);
    }

    @Test
    public void whereClauseTest() throws SQLException, RuntimeException, AnnoException {
        int adminsCount = userRepo.where("login").like("admin").getCount();
        assertEquals(adminsCount, 1);
        User[] users = userRepo.where("login").like("admin").get();
        User admin = users[0];
        assertTrue(admin.getId() == 1);
        assertTrue(admin.getLogin().trim().equals("admin"));
        assertTrue(admin.getFullName() != null);
        assertTrue(admin.getPasswordHash() != null);
        assertTrue(admin.getFullName() != null);
        assertTrue(admin.getState() != null);

        
        User user = userRepo.where("login").equal(null).getFirst();
        assertNull(user);

        userRepo.count();
    }

    @Test
    public void whereClauseEdgeCases() {
        User user = null;
        try {
            user = userRepo.where("login").less(12).getFirst();
        } catch(Throwable ex) {
            assertTrue(ex instanceof SQLException);
        }
        
        try {
            user = userRepo.where("login").greater("asd").getFirst();
        } catch (Throwable e) {
            assertTrue(e instanceof SQLException);
        }

        try {
            user = userRepo.where("login").and("state").getFirst();
        } catch (Throwable e) {
            assertTrue(e instanceof RuntimeException);
        }
        try {
            user = userRepo.where("login").or("state").getFirst();
        } catch (Throwable e) {
            assertTrue(e instanceof RuntimeException);
        }
        try {
            user = userRepo.where("login").getFirst();
        } catch (Throwable e) {
            assertTrue(e instanceof RuntimeException);
        }
    }
}
