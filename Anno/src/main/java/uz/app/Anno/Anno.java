package uz.app.Anno;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;

import uz.app.Anno.orm.*;
import uz.app.Anno.orm.annotations.Column;
import uz.app.Anno.orm.annotations.Id;
import uz.app.Anno.orm.annotations.Schema;
import uz.app.Anno.orm.annotations.Table;
import uz.app.Anno.service.BaseService;
import uz.app.Anno.service.annotations.Service;
import uz.app.AnnoDBC.PoolConnection;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

public class Anno {
    static HashMap<String, BaseService> annoServices;
    static HashMap<Class<? extends BaseEntity>, EntityMetaData> EntityMDCache;
    static HashMap<String, TableMetaData> TableMDCache;

    public static class TableMetaData
    {
        String TABLE_NAME;
        String SCHEMA_NAME;

        HashMap<Integer, String> Naming;
        HashMap<String, Integer> ColumnType;
        LinkedList<String> NullableColumns;
        LinkedList<String> GeneratedColumns;
        LinkedList<String> AutoincrementColumns;


        public TableMetaData(String tableName, String schemaName)
        {
            this.TABLE_NAME = tableName;
            this.SCHEMA_NAME = schemaName;
            Naming = new HashMap<Integer, String>();
            NullableColumns = new LinkedList<String>();
            GeneratedColumns = new LinkedList<String>();
            AutoincrementColumns = new LinkedList<String>();
            ColumnType = new HashMap<String, Integer>();

            Connection conn = null;
            DatabaseMetaData metadata;
            ResultSet rs;
            try {
                PoolConnection poolConnection = AnnoContext.getPoolConnection();
                conn = poolConnection.getConnection();
                metadata = conn.getMetaData();
                rs = metadata.getColumns(null, SCHEMA_NAME, TABLE_NAME, "%");
                while(rs.next()) {
                    Integer ordinal = rs.getInt("ORDINAL_POSITION");
                    String colName = rs.getString("COLUMN_NAME");
                    Naming.put(ordinal, colName);
                    if(rs.getString("IS_GENERATEDCOLUMN").equals("YES"))
                        GeneratedColumns.add(colName);

                    if(rs.getString("IS_AUTOINCREMENT").equals("YES"))
                        AutoincrementColumns.add(colName);

                    if(rs.getString("IS_NULLABLE").equals("YES"))
                        NullableColumns.add(colName);

                    ColumnType.put(colName, rs.getInt("DATA_TYPE"));
                }
                poolConnection.close(conn);
            } catch (SQLException ex) {
                ex.printStackTrace();
                return; // Couldn't connect to database. Refreshing stopped.
            } finally {
            }
        }

        public boolean isNullable(String column)
        {
            return NullableColumns.contains(column);
        }

        public boolean isGenerated(String column)
        {
            return GeneratedColumns.contains(column);
        }

        public boolean isAutoincrement(String column)
        {
            return AutoincrementColumns.contains(column);
        }

        public boolean hasColumn(String column)
        {
            return Naming.values().contains(column);
        }

        public Integer getDataType(String column)
        {
            return ColumnType.get(column);
        }

        public Collection<String> getNullable()
        {
            return new LinkedList<>(NullableColumns);
        }

        public Collection<String> getGenerated()
        {
            return new LinkedList<>(GeneratedColumns);
        }

        public Collection<String> getAutoincrement()
        {
            return new LinkedList<>(AutoincrementColumns);
        }

        public Collection<String> getColumnsNames()
        {
            return Naming.values();
        }
    }

    public static class EntityMetaData
    {
        Class<? extends BaseEntity> ENTITY_CLASS;
        public EntityMetaData(Class<? extends BaseEntity> entityClass)
        {
            this.ENTITY_CLASS = entityClass;
        }

        public LinkedList<Field> getAllFields()
        {
            LinkedList<Field> res = new LinkedList<Field>();
            Field[] fields = ENTITY_CLASS.getDeclaredFields();
            for(int i = 0; i < fields.length; i++)
            {
                res.add(fields[i]);
            }
            return res;
        }

        public String getColumnName(Field field)
        {
            if(field == null)
                return "";

            return field.getAnnotation(Column.class).value();
        }

        public Field getFieldByColumnName(String columnName)
        {
            Field[] fields = ENTITY_CLASS.getDeclaredFields();
            for(Field field : fields)
            {
                String currColumn = field.getAnnotation(Column.class).value();
                if(columnName.equals(currColumn))
                {
                    field.setAccessible(true);
                    return field;
                }
            }
            return null;
        }

        public Field getIdField()
        {
            Field res = null;
            Field[] fields = ENTITY_CLASS.getDeclaredFields();
            for (Field field: fields) {
                if(field.getAnnotation(Id.class) != null)
                {
                    res = field;
                    break;
                }
            }
            return res;
        }

        public String getIdColumnName()
        {
            return getColumnName(getIdField());
        }

        public String getTableFullName()
        {
            return getSchemaName() + ".\"" + getTableName() + "\"";
        }

        public String getTableName()
        {
            return this.ENTITY_CLASS.getAnnotation(Table.class).value();
        }

        public String getSchemaName()
        {
            return this.ENTITY_CLASS.getAnnotation(Schema.class).value();
        }
    }

    public static TableMetaData forTable(String tableName)
    {
        return forTable(tableName, "public");
    }

    public static TableMetaData forTable(String tableName, String schemaName)
    {
        String tableFullName = schemaName + "." + tableName;
        if(TableMDCache.keySet().contains(tableFullName))
            return TableMDCache.get(tableFullName);

        TableMetaData tmd = new TableMetaData(tableName, schemaName);
        TableMDCache.put(tableFullName, tmd);

        return tmd;
    }

    public static EntityMetaData forEntity(Class<? extends BaseEntity> entityClass)
    {
        if(EntityMDCache.keySet().contains(entityClass))
            return EntityMDCache.get(entityClass);

        EntityMetaData emd = new EntityMetaData(entityClass);
        EntityMDCache.put(entityClass, emd);

        return emd;
    }

    static void initializeServices()
    {
        annoServices = new HashMap<String, BaseService>();
        System.out.print("Initializing service");
        Reflections reflections = new Reflections("", new SubTypesScanner());
        Set<Class<? extends BaseService>> serviceClasses = reflections.getSubTypesOf(BaseService.class);
        for (Class<? extends BaseService> serviceClass : serviceClasses) {
            Service serviceAnnotation = serviceClass.getAnnotation(Service.class);
            String serviceName = "";
            if(serviceAnnotation != null)
                serviceName = serviceAnnotation.value();

            try {
                BaseService service = serviceClass.getConstructor().newInstance();
                annoServices.put(serviceName, service);
            } catch(Exception ex){
                ex.printStackTrace();
            }
        }
    }

    public static void Init()
    {
        EntityMDCache = new HashMap<Class<? extends BaseEntity>, EntityMetaData>();
        TableMDCache = new HashMap<String, TableMetaData>();
        AnnoEventListener.collectListeners();
        AnnoEventListener.triggerBeforeServicesInitializing();
        initializeServices();
        AnnoEventListener.triggerAfterServicesInitialized();
        AnnoEventListener.triggerAfterAnnoInitialized();
    }

}
