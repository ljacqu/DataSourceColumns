# Generic data source columns

[![Build Status](https://travis-ci.org/ljacqu/DataSourceColumns.svg?branch=master)](https://travis-ci.org/ljacqu/DataSourceColumns) [![Coverage Status](https://coveralls.io/repos/github/ljacqu/DataSourceColumns/badge.svg)](https://coveralls.io/github/ljacqu/DataSourceColumns)

Generates SQL code to insert, update and count values with database columns whose configurations are loaded at runtime.
It can be defined at runtime what name a column actually has in the SQL database, or whether the column's existence
should be ignored altogether.

The following properties of any column can be configured:

- **name**: The actual name the column has in the database
- **optional**: Whether the column is used at all, or if its existence should be ignored
- **defaultForNull**: Whether the _default value_ of a SQL column should be used if _null_ is given as its value (for writing)

This allows the developer to write logic once, without having to explicitly handle various situations
(e.g. _what if this column is ignored?_, _what if we need to use DEFAULT when a value is null?_):

```java
// modify entry with ID 2315
int id = 2315;

columnsHandler.update(id, UpdateValues.with(AppColumns.IS_ACTIVE, true)
  .and(AppColumns.SECRET_KEY, null).build());
```

Depending on the configuration, the above code might generate any of the following SQL codes:

```sql
UPDATE table
SET is_active = true, secret_key = NULL
WHERE id = 2315;
```

```sql
UPDATE table
SET is_active = true, secret_key = DEFAULT
WHERE id = 2315;
```

```sql
UPDATE table
SET secret_key = NULL
WHERE id = 2315;
```

```sql
UPDATE table
SET is_active = true
WHERE id = 2315;
```