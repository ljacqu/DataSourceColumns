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

## Features
### Configure column name at runtime
The method `Column#resolveName(C context)` is how a column's actual name in the database is retrieved.
This way, column names can be easily configured (e.g. to hook into an existing database).

Consider the following Java code:
```java
int id = 324;
DataSourceValues values = handler.retrieve(id, AppColumns.IS_ACTIVE, AppColumns.SECRET_KEY);
```
To generate the SQL code, `resolveName()` will be called on `AppColumns.IS_ACTIVE` and `AppColumns.SECRET_KEY`.
Depending on the results, this may yield any of the following SQL lines below (or anything else, really...):
```sql
SELECT is_active, secret_key FROM table WHERE id = 324;
SELECT active, secret FROM table WHERE id = 324;
SELECT isActive, userSecret FROM table WHERE id = 324;
-- and so forth...
```

#### Limitation
Column names are neither escaped nor validated!

### Optional columns
With `Column#isColumnUsed` one can define whether the column should be ignored altogether when SQL code is generated.

```java
// given AppColumns.NAME and AppColumns.IP, whereby AppColumns.IP#isColumnUsed returns false:

int id = 393;
DataSourceValues values = handler.retrieve(id, AppColumns.NAME, AppColumns.IP);

values.get(Columns.NAME); // returns the name column
values.get(Columns.IP);   // returns null -> column was ignored (e.g. it doesn't exist)
```
Generates the SQL code:
```sql
SELECT name FROM table WHERE id = 393;
```

#### Limitation
Performing an insert operation with columns that all return that they should be skipped will result in an exception.
Retrieving or updating all-optional columns is fine.

### Use DEFAULT for null values
When hooking into an existing database, it may be that the database you are hooking in doesn't allow you to store NULL
values. The next best thing, although ugly, is to set a default value in the column definition and to use that as a
fallback. The `ColumnsHandler` deals with these situations for you so you don't need to pollute your application with
that logic.

If `Column#useDefaultForNullValue` returns `true`, the SQL `DEFAULT` keyword will be used whenever the column is
associated with `null` as value. For example:

```java
// Assuming that only AppColumns.IP#useDefaultForNullValue returns true

int id = 235;
handler.update(id, UpdateValues.with(AppColumns.IP, null).and(AppColumns.NAME, null).build());
```

would generate the following SQL code:
```sql
UPDATE table SET ip = DEFAULT, name = NULL WHERE id = 235;
```

#### Limitation
Not all database engines support `DEFAULT` (e.g. SQLite does not). Retrieving values from the database has no regard for
this "default-for-null" setting, i.e. default values are retrieved from the database without any modification.

### Predicates
You may want to filter and count rows that fulfill some conditions. This can be achieved by passing a predicate to the
_columns handler_, which will consider the options discussed above. Example:

```java
Predicate predicate = eq(AppColumns.NAME, "test").and(lessThan(AppColumns.HOURS_ONLINE, 3));
int count = handler.count(predicate);
```

Depending on the configuration of the columns, this might generate SQL code similar to the examples given below.
```sql
SELECT COUNT(1) FROM table WHERE name = 'test' AND hours_online < 3;
```

```sql
SELECT COUNT(1) FROM table WHERE name = 'test'; -- HOURS_ONLINE should not be used
```