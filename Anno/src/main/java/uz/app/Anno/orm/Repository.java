package uz.app.Anno.orm;

import uz.app.Anno.Anno;
import uz.app.Anno.AnnoContext;
import uz.app.AnnoDBC.PoolConnection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.LinkedList;

public class Repository<T extends BaseEntity> {
    public static LinkedList<Repository> Instances;

    static {
        Instances = new LinkedList<>();
    }

    protected String TABLE_NAME = "";
    protected String SCHEMA_NAME = "public";

    protected Class<T> ClassRef = null;
    protected Field idField;
    protected LinkedList<Field> entityFields;
    protected PoolConnection poolConnection;
    private Constructor constructor = null;

    public Repository()
    {
        Instances.add(this);
        entityFields = new LinkedList<>();
    }

    protected void SetTargetEntity(Class<T> cl)
    {
        ClassRef = cl;
    }

    public void Init() throws Exception
    {
        if(ClassRef == null)
            throw new Exception("Target entity not set for repository");

        poolConnection = AnnoContext.getPoolConnection();
        Constructor ctor = null;
        try{
            Constructor ctors[] = ClassRef.getDeclaredConstructors();
            for (Constructor c: ctors)
                if (c.getGenericParameterTypes().length == 0)
                {
                    ctor = c;
                    break;
                }

            if(ctor == null)
                throw new Exception("Error: constructor with no arguments not found. Class name: " + getClass().getName());

            this.constructor = ctor;
            this.constructor.setAccessible(true);

            Annotation SchemaAnno = ClassRef.getAnnotation(Schema.class);
            Annotation TableAnno = ClassRef.getAnnotation(Table.class);
            this.idField = Anno.forEntity(ClassRef).getIdField();


            if(SchemaAnno == null) {
                throw new NullPointerException("Schema's name must be indicated for entity class <" + ClassRef.getName() + ">");
            }
            if(TableAnno == null) {
                throw new NullPointerException("Table's name must be indicated for entity class <" + ClassRef.getName() + ">");
            }
            if(this.idField == null)
                throw new NullPointerException("Id field must be indicated for entity class <" + ClassRef.getName() + ">");

            this.SCHEMA_NAME = Anno.forEntity(ClassRef).getSchemaName();
            this.TABLE_NAME = Anno.forEntity(ClassRef).getTableName();
            entityFields = Anno.forEntity(ClassRef).getAllFields();

        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    public T[] getAll() throws SQLException
    {
        Connection connection = poolConnection.getConnection();
        if(connection == null)
            throw new SQLException("Couldn't connect to database.");

        T[] res;
        LinkedList<T> entities = new LinkedList<T>();
        PreparedStatement stmt = connection.prepareStatement("SELECT * FROM " +
                Anno.forEntity(ClassRef).getTableFullName());

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

    public T getById(long id) throws SQLException
    {
        Connection connection = poolConnection.getConnection();
        if(connection == null)
            throw new SQLException("Couldn't connect to database.");

        T entity;
        StringBuilder query = new StringBuilder();
        query.append("SELECT * FROM ")
                .append(Anno.forEntity(ClassRef).getTableFullName())
                .append(" WHERE ")
                .append(Anno.forEntity(ClassRef).getIdColumnName())
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

    public void save(T entity) throws SQLException, AnnoValidationException
    {
        Connection connection = poolConnection.getConnection();
        if(connection == null)
            throw new SQLException("Couldn't connect to database.");

        if(entity == null)
            return;

        entity.validate();

        /*for (Field field : entityFields)
        {
            field.setAccessible(true);
            if(field.get(entity) != null)
                System.out.println(field.getName() + " : " + field.get(entity).toString());
            else
                System.out.println(field.getName() + " : null");
        }*/

        //  INSERT INTO public."Users"("login", "fullName", "email", "passwordHash", "state")
        //	VALUES ('Vasya_gq', 'Vasya Ivanov', 'v.ivanov@meyl.ru', '$2y$12$ALkeFSdcN7o.JAY/e9z7VePMLD7WWJYDAbVyknB/tG40BWP.tgnh6', 'A');

        String fields = getEntityFields();
        String valuesPlaceholder = getEntityValuePlaceholders();
        StringBuilder SqlQuery = new StringBuilder("INSERT INTO ");
        SqlQuery.append(Anno.forEntity(ClassRef).getTableFullName())
                .append(" (")
                .append(fields)
                .append(") VALUES (")
                .append(valuesPlaceholder)
                .append(");");


        PreparedStatement stmt = connection.prepareStatement(SqlQuery.toString());

        Integer SqlType;
        int index = 1;
        for (Field field : entityFields)
        {
            String colName = Anno.forEntity(ClassRef).getColumnName(field);
            if(!Anno.forTable(TABLE_NAME, SCHEMA_NAME).hasColumn(colName)
                    || Anno.forTable(TABLE_NAME, SCHEMA_NAME).isGenerated(colName)
                    || Anno.forTable(TABLE_NAME, SCHEMA_NAME).isAutoincrement(colName)) // exclude generated and autoincrement fields
                continue;

            field.setAccessible(true);
            Object value;
            try { value = field.get(entity); } catch (IllegalAccessException ex) { ex.printStackTrace(); value = ""; }

            SqlType = Anno.forTable(TABLE_NAME, SCHEMA_NAME).getDataType(colName);
            stmt.setObject(index, value, SqlType);

            index++;
        }
        int affectedRowsCtn = 0;
        try {
            affectedRowsCtn  = stmt.executeUpdate();
            stmt.close();
        } finally {
            poolConnection.close(connection);
        }

        if(affectedRowsCtn == 0)
            throw new SQLException("SQL: No rows affected");
    }

    public void delete(long id) throws SQLException
    {
        Connection connection = poolConnection.getConnection();
        if(connection == null)
            throw new SQLException("Couldn't connect to database.");

        PreparedStatement stmt = connection.prepareStatement("DELETE FROM " +
                Anno.forEntity(ClassRef).getTableFullName() +
                " WHERE " +
                Anno.forEntity(ClassRef).getIdColumnName() +
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
        long res = 0;
        Connection connection = poolConnection.getConnection();
        if(connection == null)
            throw new SQLException("Couldn't connect to database.");

        PreparedStatement stmt = connection.prepareStatement("SELECT count(*) FROM " +
                Anno.forEntity(ClassRef).getTableFullName());
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

    

    
    /**
     * Instantiates target entity of repository, filling with data from given result-set 
     * @param rs Result-set of query
     * @param rsmd Meta-data of target entity.
     * @return Instance of target entity
     * @throws SQLException
     */
    protected final T makeObject(ResultSet rs, ResultSetMetaData rsmd) throws SQLException
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
            Field field = Anno.forEntity(cl).getFieldByColumnName(colName);
            int colType = rsmd.getColumnType(colIndex);

            if(field == null)
                continue;
            field.setAccessible(true);

            try {
                switch (colType) {
                    case Types.VARCHAR:
                    case Types.LONGVARCHAR:
                    case Types.CHAR:
                    case Types.NCHAR:
                    case Types.NVARCHAR:
                        field.set(obj, rs.getString(colIndex).trim());
                        break;
                    case Types.INTEGER:
                    case Types.NUMERIC:
                        field.setInt(obj, rs.getInt(colIndex));
                        break;
                    case Types.BIGINT:
                        field.setLong(obj, rs.getLong(colIndex));
                        break;
                    case Types.SMALLINT:
                    case Types.TINYINT:
                        field.setShort(obj, rs.getShort(colIndex));
                        break;
                    case Types.REAL:
                        field.setFloat(obj, rs.getFloat(colIndex));
                        break;
                    case Types.DOUBLE:
                    case Types.DECIMAL:
                        field.setDouble(obj, rs.getDouble(colIndex));
                        break;
                    case Types.TIME:
                        field.set(obj, rs.getTime(colIndex));
                        break;
                    case Types.TIMESTAMP:
                        field.set(obj, rs.getTimestamp(colIndex));
                        break;
                    case Types.DATE:
                        field.set(obj, rs.getDate(colIndex));
                        break;
                    default:
                        field.set(obj, rs.getObject(colIndex));
                        break;
                }
            } catch (IllegalAccessException ex) {
                ex.printStackTrace();
            }
        }
        return (T)obj;
    }

    /**
     * Writes comma separated column names
     * @return Comma separated column names
     */
    protected String getEntityFields()
    {
        StringBuilder fieldsList = new StringBuilder();
        for (Field field: entityFields)
        {
            String colName = Anno.forEntity(ClassRef).getColumnName(field);
            if(!Anno.forTable(TABLE_NAME, SCHEMA_NAME).hasColumn(colName)
                    || Anno.forTable(TABLE_NAME, SCHEMA_NAME).isGenerated(colName)
                    || Anno.forTable(TABLE_NAME, SCHEMA_NAME).isAutoincrement(colName)) // exclude generated and autoincrement fields
                continue;

            fieldsList.append('"');
            fieldsList.append(colName);
            fieldsList.append('"');
            fieldsList.append(',');
        }
        return fieldsList.substring(0, fieldsList.length()-1);  // returning without last comma
    }

    protected String getEntityValuePlaceholders()
    {
        StringBuilder strBld = new StringBuilder();
        for (Field field : entityFields)
        {
            String colName = Anno.forEntity(ClassRef).getColumnName(field);
            if(!Anno.forTable(TABLE_NAME, SCHEMA_NAME).hasColumn(colName)
                    || Anno.forTable(TABLE_NAME, SCHEMA_NAME).isGenerated(colName)
                    || Anno.forTable(TABLE_NAME, SCHEMA_NAME).isAutoincrement(colName)) // exclude generated and autoincrement fields
                continue;
            strBld.append("?,");
        }

        return strBld.substring(0, strBld.length()-1);
    }
}
