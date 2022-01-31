package uz.app.Anno.test;

import uz.app.Anno.orm.*;
import uz.app.Anno.util.*;

@Schema("public")
@Table("Users")
public class User extends BaseEntity {
    @Id
    @Generated
    @Column("id")
    private long id;
    @Column("login")
    private String login;
    @Column("fullName")
    private String fullName;
    @Column("email")
    private String email;
    @Column("passwordHash")
    private String passwordHash;
    @Column("state")
    private String state;

    public User() { }

    public User(int id, String login, String fullName, String email, String passwordHash, String state) {
        this.id = id;
        this.login = login;
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.state = state;
    }

    @Override
    public boolean isValid() {
        if(!Rgx.isEmail(this.email))
            return false;

        if(this.fullName == null || this.fullName.length() == 0)
            return false;

        if(this.login == null || this.login.length() == 0)
            return false;

        if(this.passwordHash == null || this.passwordHash.length() == 0)
            return false;

        if(this.state == null || this.state.length() == 0)
            this.state = "A";

        return true;
    }

    @Override
    public void validate() throws AnnoValidationException {
        if(!Rgx.isEmail(this.email))
            throw new AnnoValidationException("14010", "Invalid email");

        if(this.fullName == null || this.fullName.length() == 0)
            throw new AnnoValidationException("14020", "Invalid full name: empty");

        if(this.login == null || this.login.length() == 0)
            throw new AnnoValidationException("14030", "Invalid login: empty");

        if(this.passwordHash == null || this.passwordHash.length() == 0)
            throw new AnnoValidationException("14040", "Invalid password hash: empty");

        if(this.state == null || this.state.length() == 0)
            this.state = "A";
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}

