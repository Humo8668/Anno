package uz.app.Anno.orm;

import uz.app.Anno.orm.annotations.Schema;
import uz.app.Anno.orm.annotations.Table;
import uz.app.Anno.orm.exceptions.AnnoException;
import uz.app.Anno.orm.exceptions.ValidationException;
import uz.app.Anno.util.PoolConnection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;

public class Repository<T extends IEntity> {
    public static LinkedList<Repository<? extends IEntity>> Instances;

    static {
        Instances = new LinkedList<>();
    }

    protected String TABLE_NAME = "";
    protected String SCHEMA_NAME = "public";

    protected Class<T> ClassRef = null; // class of entity behind of this repository works
    protected Field idField;
    protected List<Field> entityFields;
    protected List<Field> entityColumns;
    protected OrmContext context;
    protected PoolConnection poolConnection;
    protected Metadata metadata;
    private Constructor<T> constructor = null;

    protected Repository(OrmContext context) throws SQLException
    {
        this.context = context;
        poolConnection = context.getPoolConnection(); 
        metadata = context.getMetadata();
        Instances.add(this);
        entityFields = new LinkedList<>();
        entityColumns = new LinkedList<>();
    }

    protected void SetTargetEntity(Class<T> cl) throws AnnoException
    {
        if(cl == null)
            throw new NullPointerException("Target entity not set for repository");
        ClassRef = cl;
        Constructor<T> ctor = null;
        Constructor<T> ctors[] = (Constructor<T>[])ClassRef.getDeclaredConstructors();
        for (Constructor<T> c: ctors)
            if (c.getGenericParameterTypes().length == 0)
            {
                ctor = c;
                break;
            }

        if(ctor == null)
            throw new AnnoException("Error: constructor with no arguments not found. Class name: " + getClass().getName());

        this.constructor = ctor;
        this.constructor.setAccessible(true);

        Annotation SchemaAnno = ClassRef.getAnnotation(Schema.class);
        Annotation TableAnno = ClassRef.getAnnotation(Table.class);
        this.idField = metadata.forEntity(ClassRef).getIdField();


        if(SchemaAnno == null) {
            throw new NullPointerException("Schema's name must be indicated for entity class <" + ClassRef.getName() + ">");
        }
        if(TableAnno == null) {
            throw new NullPointerException("Table's name must be indicated for entity class <" + ClassRef.getName() + ">");
        }
        if(this.idField == null)
            throw new NullPointerException("Id field must be indicated for entity class <" + ClassRef.getName() + ">");

        this.SCHEMA_NAME = metadata.forEntity(ClassRef).getSchemaName(context.getDatabaseDefaultSchema());
        this.TABLE_NAME = metadata.forEntity(ClassRef).getTableName();
        entityFields = metadata.forEntity(ClassRef).getAllFields();
        entityColumns = metadata.forEntity(ClassRef).getColumnAnnotatedFields();
    }

    protected static Object selectFromDumb(Connection conn, String expression) throws SQLException {
        Object result;
        PreparedStatement stmt = conn.prepareStatement("SELECT " + expression);
        ResultSet rs = stmt.executeQuery();
        if(rs.next()) {
            result = rs.getObject(1);
        } else {
            result = null;
        }

        return result;
    }

    

    public T[] getAll() throws SQLException, AnnoException
    {
        if(ClassRef == null)
            return null;
        Connection connection = poolConnection.getConnection();
        if(connection == null)
            throw new SQLException("Couldn't connect to database.");

        T[] res;
        LinkedList<T> entities = new LinkedList<T>();
        PreparedStatement stmt = connection.prepareStatement("SELECT * FROM " +
            metadata.forEntity(ClassRef).getTableFullName());

        try{
            ResultSet rs = stmt.executeQuery();
            ResultSetMetaData rsmd = rs.getMetaData();
            T entity;
            while(rs.next())
            {
                entity = this.makeObject(rs, rsmd);
                entities.add(entity);
            }

            int i = 0;
            res = (T[])Array.newInstance(ClassRef, entities.size());
            for (T e: entities) {
                if(i > entities.size())
                    break;
                res[i] = e;
                i++;
            }
            rs.close();
        } finally {
            poolConnection.close(connection);
        }

        return entities.toArray(res);
    };

    /**
     * Retrieves an entity from database by Id.
     * @param id Value for compare with id-column
     * @return Object of entity.
     * @throws SQLException If connection to database is closed or there's some issue with connection
     * @throws AnnoException
     */
    public T getById(long id) throws SQLException, AnnoException
    {
        if(ClassRef == null)
            return null;
        Connection connection = poolConnection.getConnection();
        if(connection == null)
            throw new SQLException("Couldn't connect to database.");

        T entity;
        StringBuilder query = new StringBuilder();
        query.append("SELECT * FROM ")
                .append(metadata.forEntity(ClassRef).getTableFullName())
                .append(" WHERE ")
                .append(metadata.forEntity(ClassRef).getIdColumnName())
                .append(" = ?");

        PreparedStatement stmt = connection.prepareStatement(query.toString());
        stmt.setLong(1, id);
        try {
            ResultSet rs = stmt.executeQuery();
            ResultSetMetaData rsmd = rs.getMetaData();

            if(rs.next())   // If row exists
                entity = this.makeObject(rs, rsmd);
            else
                entity = null;
            rs.close();
            stmt.close();
        } finally {
            poolConnection.close(connection);
        }

        return entity;
    }






    public void save(T entity) throws SQLException, ValidationException, AnnoException
    {
        if(ClassRef == null)
            return ;

        Connection connection = poolConnection.getConnection();
        if(connection == null)
            throw new SQLException("Couldn't connect to database.");

        if(entity == null)
            return;

        entity.validate();

        //  INSERT INTO public."Users"("login", "fullName", "email", "passwordHash", "state")
        //	VALUES ('Vasya_gq', 'Vasya Ivanov', 'v.ivanov@meyl.ru', '$2y$12$ALkeFSdcN7o.JAY/e9z7VePMLD7WWJYDAbVyknB/tG40BWP.tgnh6', 'A');

        String columns = getEntityColumns();
        String valuesPlaceholder = getEntityValuePlaceholders();
        StringBuilder SqlQuery = new StringBuilder("INSERT INTO ");
        SqlQuery.append(metadata.forEntity(ClassRef).getTableFullName())
                .append(" (")
                .append(columns)
                .append(") VALUES (")
                .append(valuesPlaceholder)
                .append(");");


        PreparedStatement stmt = connection.prepareStatement(SqlQuery.toString(), Statement.RETURN_GENERATED_KEYS);

        Integer SqlType;
        int index = 1;
        for (Field field : entityColumns)
        {

            String colName = metadata.forEntity(ClassRef).getColumnName(field);
            if(!metadata.forTable(TABLE_NAME, SCHEMA_NAME).hasColumn(colName)
                    || metadata.forTable(TABLE_NAME, SCHEMA_NAME).isGenerated(colName)
                    || metadata.forTable(TABLE_NAME, SCHEMA_NAME).isAutoincrement(colName)) // exclude generated and autoincrement fields
                continue;

            field.setAccessible(true);
            Object value;
            String defaultValue = metadata.forTable(TABLE_NAME, SCHEMA_NAME).getDefaultValue(colName);
            try { value = field.get(entity); } catch (IllegalAccessException ex) { ex.printStackTrace(); value = ""; }
            if(value == null || "".equals(value))
                value = Repository.selectFromDumb(connection, defaultValue);
            SqlType = metadata.forTable(TABLE_NAME, SCHEMA_NAME).getDataType(colName);
            stmt.setObject(index, value, SqlType);
            field.setAccessible(false);

            index++;
        }
        int affectedRowsCtn = 0;

        // !!! TO DO: return generated keys to syncronously update Java-object.
        try {
            affectedRowsCtn = stmt.executeUpdate();
            ResultSet genKeysRs = stmt.getGeneratedKeys();
            ResultSetMetaData genKeysRsmd =  genKeysRs.getMetaData();
            int count = genKeysRsmd.getColumnCount();
            if(genKeysRs.next()) { // if row inserted
                for(int i = 1; i <= count; i++) { // for each generated column
                    String generatedColumnName = genKeysRsmd.getColumnName(i);
                    Object generatedValue = genKeysRs.getObject(i);

                    Field genKeyField = metadata.forEntity(ClassRef).getFieldByColumnName(generatedColumnName);
                    try {
                        metadata.forEntity(ClassRef).setValueForField(genKeyField, entity, generatedValue);
                    } catch (IllegalArgumentException e) {
                        throw new AnnoException("Wrong typed entity for column: " + generatedColumnName + "; Table: " + metadata.forEntity(ClassRef).getTableFullName());
                    } catch (IllegalAccessException e) {
                        (new AnnoException(
                            "Couldn't access to field of entity " + ClassRef.getName() + 
                            "; Field: " + genKeyField.getName()
                        )).printStackTrace();
                    }catch (InvocationTargetException e1) {
                        throw new AnnoException("Exception on setting value throught setter.", e1);
                    }
                }
            }
            genKeysRs.close();
            stmt.close();
        } finally {
            poolConnection.close(connection);
        }

        if(affectedRowsCtn == 0)
            throw new SQLException("SQL: No rows affected");
    }




    

    public void update(T entity) throws SQLException, ValidationException, AnnoException
    {
        if(ClassRef == null)
            return ;

        Connection connection = poolConnection.getConnection();
        if(connection == null)
            throw new SQLException("Couldn't connect to database.");

        if(entity == null)
            return;

        entity.validate();
        if(metadata.forEntity(entity.getClass()).getIdValue(entity) == null)
            throw new ValidationException("Set value for unique(`Id` or something) field", metadata.forEntity(ClassRef).getIdColumnName());

        StringBuilder SqlQuery = new StringBuilder("UPDATE ");
        SqlQuery.append(metadata.forEntity(ClassRef).getTableFullName())
                .append(" SET ");

        for (Field field : entityColumns)
        {
            String colName = metadata.forEntity(ClassRef).getColumnName(field);
            if(!metadata.forTable(TABLE_NAME, SCHEMA_NAME).hasColumn(colName)
                    || metadata.forTable(TABLE_NAME, SCHEMA_NAME).isGenerated(colName)
                    || metadata.forTable(TABLE_NAME, SCHEMA_NAME).isAutoincrement(colName)) // exclude generated and autoincrement fields
                continue;
            SqlQuery.append("\"").append(colName).append("\"").append(" = ").append("? ,");
        }
        SqlQuery.deleteCharAt(SqlQuery.length() - 1);// slice the last comma
        SqlQuery.append(" where ").append(metadata.forEntity(ClassRef).getIdColumnName()).append(" = ").append("?");
        
        Integer SqlType;
        int index = 1;
        PreparedStatement stmt = connection.prepareStatement(SqlQuery.toString());
        for(Field field :entityColumns)
        {
            Object value;
            String colName = metadata.forEntity(ClassRef).getColumnName(field);
            if(!metadata.forTable(TABLE_NAME, SCHEMA_NAME).hasColumn(colName)
                    || metadata.forTable(TABLE_NAME, SCHEMA_NAME).isGenerated(colName)
                    || metadata.forTable(TABLE_NAME, SCHEMA_NAME).isAutoincrement(colName)) // exclude generated and autoincrement fields
                continue;

            SqlType = metadata.forTable(TABLE_NAME, SCHEMA_NAME).getDataType(colName);
            field.setAccessible(true);
            String defaultValue = metadata.forTable(TABLE_NAME, SCHEMA_NAME).getDefaultValue(colName); // calculate default value from sql default expression 
            try { value = field.get(entity); } catch (IllegalAccessException ex) { ex.printStackTrace(); value = ""; } // First, try to get field value. If couldn't get, then set empty string.
            if(value == null || "".equals(value))   // If value for column is null or empty string then put default value to column. 
                value = Repository.selectFromDumb(connection, defaultValue);
            stmt.setObject(index, value, SqlType);
            field.setAccessible(false);

            index++;
        }

        // ****** Set value for id in where-clause of query ********
        Object entityId = metadata.forEntity(entity.getClass()).getIdValue(entity);
        SqlType = metadata.forTable(TABLE_NAME, SCHEMA_NAME).getDataType(metadata.forEntity(ClassRef).getIdColumnName());
        metadata.forEntity(ClassRef).getIdField().setAccessible(true);
        stmt.setObject(index, entityId, SqlType);
        metadata.forEntity(ClassRef).getIdField().setAccessible(false);
        // *********************************************************

        //int affectedRowsCtn = 0;
        try {
            stmt.executeUpdate();
            ResultSet genKeysRs = stmt.getGeneratedKeys();
            ResultSetMetaData genKeysRsmd =  genKeysRs.getMetaData();
            int count = genKeysRsmd.getColumnCount();
            while(genKeysRs.next()) { // for each updated row
                Object updatedRowId = genKeysRs.getObject(metadata.forEntity(ClassRef).getIdColumnName());
                if(entityId.equals(updatedRowId)) {
                    for(int i = 1; i <= count; i++) { // for each updated column
                        String generatedColumnName = genKeysRsmd.getColumnName(i);
                        Object generatedValue = genKeysRs.getObject(i);

                        Field genKeyField = metadata.forEntity(ClassRef).getFieldByColumnName(generatedColumnName);
                        try {
                            metadata.forEntity(ClassRef).setValueForField(genKeyField, entity, generatedValue);
                        } catch (IllegalAccessException e1) {
                            (new AnnoException(
                                "Couldn't access to field of entity " + ClassRef.getName() + 
                                "; Field: " + genKeyField.getName()
                            )).printStackTrace();
                        } catch (IllegalArgumentException e1) {
                            throw new AnnoException("Wrong typed entity for column: " + generatedColumnName + "; Table: " + metadata.forEntity(ClassRef).getTableFullName());
                        } catch (InvocationTargetException e1) {
                            throw new AnnoException("Exception on setting value throught setter.", e1);
                        }
                    }
                }
            }
            genKeysRs.close();
            stmt.close();
        } finally {
            poolConnection.close(connection);
        }
    }

    public void delete(long id) throws SQLException
    {
        if(ClassRef == null)
            return ;
        Connection connection = poolConnection.getConnection();
        if(connection == null)
            throw new SQLException("Couldn't connect to database.");

        PreparedStatement stmt = connection.prepareStatement("DELETE FROM " +
                metadata.forEntity(ClassRef).getTableFullName() +
                " WHERE " +
                metadata.forEntity(ClassRef).getIdColumnName() +
                " = ?");
        stmt.setLong(1, id);
        try {
            stmt.executeUpdate();
            stmt.close();
        } finally {
            poolConnection.close(connection);
        }

    }

    public long count() throws SQLException
    {
        if(ClassRef == null)
            return 0;

        long res = 0;
        Connection connection = poolConnection.getConnection();
        if(connection == null)
            throw new SQLException("Couldn't connect to database.");

        PreparedStatement stmt = connection.prepareStatement("SELECT count(*) FROM " +
                metadata.forEntity(ClassRef).getTableFullName());
        try {
            ResultSet rs = stmt.executeQuery();
            rs.next();
            res = rs.getInt(1);
            rs.close();
            stmt.close();
        }
        finally {
            poolConnection.close(connection);
        }

        return res;
    }

    public class WhereCondition
    {
        // <column> <operation> <value>
        StringBuilder whereStr;
        int state = 0; // 0 - initial, 1-expect column, 2-expect operation
        LinkedList<Object> values = new LinkedList<Object>();
        Repository<T> repository;

        protected WhereCondition(String colName, Repository<T> repository) 
        {
            this.repository = repository;
            whereStr = new StringBuilder();
            whereStr.append("\"").append(colName).append("\"").append(" ");
            state = 2;
        }
        
        public WhereCondition equal(Object value) throws RuntimeException
        {
            if(state != 2)
                throw new RuntimeException("Wrong using of where-condition. Expected operation and value");
            
            whereStr.append(" = ").append(" ? ");
            values.add(value);
            state = 1;
            return this;
        }

        public WhereCondition greater(Object value){
            if(state != 2)
                throw new RuntimeException("Wrong using of where-condition. Expected operation and value");
            
            whereStr.append(" > ").append(" ? ");
            values.add(value);
            state = 1;
            return this;
        }

        public WhereCondition less(Object value){
            if(state != 2)
                throw new RuntimeException("Wrong using of where-condition. Expected operation and value");
            
            whereStr.append(" < ").append(" ? ");
            values.add(value);
            state = 1;
            return this;
        }

        public WhereCondition like(String str) throws RuntimeException
        {
            if(state != 2)
                throw new RuntimeException("Wrong using of where-condition. Expected operation and value");
            
            whereStr.append(" like ").append(" '%'|| ? ||'%' ");
            values.add(str);
            state = 1;
            return this;
        }

        public WhereCondition or(String colName) throws RuntimeException
        {
            if(state != 1)
                throw new RuntimeException("Wrong using of where-condition. Expected column");
            whereStr.append(" or ").append("\"").append(colName).append("\"");
            state = 2;
            return this;
        }

        public WhereCondition and(String colName) throws RuntimeException
        {
            if(state != 1)
                throw new RuntimeException("Wrong using of where-condition. Expected column");
            
            whereStr.append(" and ").append("\"").append(colName).append("\"");
            state = 2;
            return this;
        }

        public T[] get() throws SQLException, AnnoException
        {
            if(state != 1)
                throw new RuntimeException("Wrong using of where-condition. Expected column");
            
            if(ClassRef == null)
                return null;
            Connection connection = poolConnection.getConnection();
            if(connection == null)
                throw new SQLException("Couldn't connect to database.");
    
            StringBuilder query = new StringBuilder();
            query.append("SELECT * FROM ")
                    .append(metadata.forEntity(ClassRef).getTableFullName())
                    .append(" WHERE ")/*
                    .append(Anno.forEntity(ClassRef).getIdColumnName())
                    .append(" = ?")*/;
            
            query.append(whereStr);

            PreparedStatement stmt = connection.prepareStatement(query.toString());
            int index = 1;
            for(Object value: values)
            {
                if(value instanceof java.util.Date) // because of some error of JDBC
                    stmt.setObject(index, value, java.sql.Types.DATE);
                else
                    stmt.setObject(index, value);//, SqlType
                index++;
            }

            T[] result;
            LinkedList<T> entities = new LinkedList<T>();
            try {
                ResultSet rs = stmt.executeQuery();
                ResultSetMetaData rsmd = rs.getMetaData();
    
                while(rs.next())   // If row exists
                    entities.add(repository.makeObject(rs, rsmd));

                rs.close();
                stmt.close();
            } finally {
                poolConnection.close(connection);
            }

            
            int i = 0;
            result = (T[])Array.newInstance(ClassRef, entities.size());
            for (T e: entities) {
                if(i > entities.size())
                    break;
                    result[i] = e;
                i++;
            }

            state = 0;
            return result;
        }

        public int getCount() throws SQLException {
            if(state != 1)
                throw new RuntimeException("Wrong using of where-condition. Expected column");
            
            if(ClassRef == null)
                throw new RuntimeException("Entity is not defined for this repository!");
            Connection connection = poolConnection.getConnection();
            if(connection == null)
                throw new SQLException("Couldn't connect to database.");

            int result;
            StringBuilder query = new StringBuilder();
            query.append("SELECT count(*) FROM ")
                    .append(metadata.forEntity(ClassRef).getTableFullName())
                    .append(" WHERE ")/*
                    .append(Anno.forEntity(ClassRef).getIdColumnName())
                    .append(" = ?")*/;
            
            query.append(whereStr);

            PreparedStatement stmt = connection.prepareStatement(query.toString());
            int index = 1;
            for(Object value: values)
            {
                if(value instanceof java.util.Date) // because of some error of JDBC
                    stmt.setObject(index, value, java.sql.Types.DATE);
                else
                    stmt.setObject(index, value);//, SqlType
                index++;
            }

            try {
                ResultSet rs = stmt.executeQuery();

                if(rs.next()) {
                    result = rs.getInt(1);
                } else {
                    throw new RuntimeException("No data received from database!");
                }
                rs.close();
                stmt.close();
            } finally {
                poolConnection.close(connection);
            }

            
            state = 0;
            return result;
        }

        public T getFirst() throws SQLException, AnnoException
        {
            if(state != 1)
                throw new RuntimeException("Wrong using of where-condition. Expected column");
            
            if(ClassRef == null)
                return null;
            Connection connection = poolConnection.getConnection();
            if(connection == null)
                throw new SQLException("Couldn't connect to database.");
    
            StringBuilder query = new StringBuilder();
            query.append("SELECT * FROM ")
                    .append(metadata.forEntity(ClassRef).getTableFullName())
                    .append(" WHERE ")/*
                    .append(Anno.forEntity(ClassRef).getIdColumnName())
                    .append(" = ?")*/;
            
            query.append(whereStr);

            PreparedStatement stmt = connection.prepareStatement(query.toString());
            int index = 1;
            for(Object value: values)
            {
                if(value instanceof java.util.Date) // because of some error of JDBC
                    stmt.setObject(index, value, java.sql.Types.DATE);
                else
                    stmt.setObject(index, value);//, SqlType
                index++;
            }

            T result = null;
            try {
                ResultSet rs = stmt.executeQuery();
                ResultSetMetaData rsmd = rs.getMetaData();
                
                if(rs.next()) {
                    result = repository.makeObject(rs, rsmd);
                }
                rs.close();
                stmt.close();
            } finally {
                poolConnection.close(connection);
            }
            return result;
        }
    }

    //repository.where("login").equals("asd").and("fullName").like("Asd").or("login").like("mark").get();
    /**
     * Represents mechanism of building SQL-where clauses and filtering rows.
     * @param columnName Name of column for which will be applied filtering.
     * @return Object of where-condition.
     */
    public WhereCondition where(String columnName)
    {
        return new WhereCondition(columnName, this);
    }

    
    /**
     * Instantiates target entity of repository, filling with data from given result-set 
     * @param rs Result-set of query
     * @param rsmd Meta-data of target entity.
     * @return Instance of target entity
     * @throws SQLException
     * @throws AnnoException
     */
    protected final T makeObject(ResultSet rs, ResultSetMetaData rsmd) throws SQLException, AnnoException
    {
        Object obj;
        try {
            obj = constructor.newInstance();
        } catch (InstantiationException|IllegalAccessException|InvocationTargetException ex) {
            ex.printStackTrace();
            return null;
        }

        Class cl = obj.getClass();
        for(int colIndex = 1; colIndex <= rsmd.getColumnCount(); colIndex++)
        {
            String colName = rsmd.getColumnName(colIndex);
            Field field = metadata.forEntity(cl).getFieldByColumnName(colName);
            int colType = rsmd.getColumnType(colIndex);

            if(field == null)
                continue;
            
            try {
                metadata.forEntity(ClassRef).setValueForField(field, obj, rs.getObject(colIndex));
            } catch (IllegalAccessException e1) {
                (new AnnoException(
                    "Couldn't access to field of entity " + ClassRef.getName() + 
                    "; Field: " + field.getName()
                )).printStackTrace();
            } catch (IllegalArgumentException e1) {
                throw new AnnoException("Wrong typed entity for column: " + colName + "; Table: " + metadata.forEntity(ClassRef).getTableFullName());
            } catch (InvocationTargetException e1) {
                throw new AnnoException("Exception on setting value throught setter.", e1);
            }
        }
        return (T)obj;
    }

    /**
     * Writes comma separated column names
     * @return Comma separated column names
     */
    protected String getEntityColumns()
    {
        StringBuilder fieldsList = new StringBuilder();
        for (Field field: entityColumns)
        {
            String colName;
            colName = metadata.forEntity(ClassRef).getColumnName(field);

            if(!metadata.forTable(TABLE_NAME, SCHEMA_NAME).hasColumn(colName)
                    || metadata.forTable(TABLE_NAME, SCHEMA_NAME).isGenerated(colName)
                    || metadata.forTable(TABLE_NAME, SCHEMA_NAME).isAutoincrement(colName)) // exclude generated and autoincrement fields
                continue;

            fieldsList.append('"');
            fieldsList.append(colName);
            fieldsList.append('"');
            fieldsList.append(',');
        }
        return fieldsList.substring(0, fieldsList.length()-1);  // returning without last comma
    }

    /**
     * Returns string of sql insert-script's value placeholder
     * @return
     */
    protected String getEntityValuePlaceholders()
    {
        StringBuilder strBld = new StringBuilder();
        for (Field field : entityColumns)
        {
            
            String colName;
            if(metadata.forEntity(ClassRef).isAnnotatedAsColumn(field)) {
                colName = metadata.forEntity(ClassRef).getColumnName(field);
            } else {
                continue;
            }
            if(!metadata.forTable(TABLE_NAME, SCHEMA_NAME).hasColumn(colName)
                    || metadata.forTable(TABLE_NAME, SCHEMA_NAME).isGenerated(colName)
                    || metadata.forTable(TABLE_NAME, SCHEMA_NAME).isAutoincrement(colName)) // exclude generated and autoincrement fields
                continue;
            strBld.append("?,");
        }

        return strBld.substring(0, strBld.length()-1);
    }
}
