# Model and QueryBuilder Documentation

## Overview
The `Model` class is an abstract, generic ORM-like class designed to simplify database interactions. It provides CRUD operations, query building, relationship handling, and caching. The `QueryBuilder` class allows constructing complex SQL queries with conditions, ordering, pagination, and eager loading of relationships.

### Key Features
- **CRUD Operations**: Save, update, delete, and fetch records.
- **Query Building**: Chain methods to build dynamic queries.
- **Relationships**: Support for `OneToMany`, `ManyToOne`, and `ManyToMany` relations.
- **Caching**: Integrated cache management for frequently accessed entities.
- **Async Operations**: Asynchronous methods for non-blocking database access.

---

## Model Class

### Setup
**Set Data Source**  
Before using models, configure the database connection pool:
```java
HikariConfig config = new HikariConfig();
config.setJdbcUrl("jdbc:postgresql://localhost/mydb");
config.setUsername("user");
config.setPassword("pass");

HikariDataSource dataSource = new HikariDataSource(config);
Model.setDataSource(dataSource);
```

### Defining a Model
Extend `Model` and define fields corresponding to your database table:
```java
public class User extends Model<User> {
  public String name;
  public String email;
}
```
Metadata (table name, fields) is auto-detected but can be customized via annotations (not shown in code).

---

### Key Methods

#### Static Methods
- **`query(Class<R> clazz)`**  
  Returns a `QueryBuilder` for the model class.  
  Example:
  ```java
  List<User> users = User.query(User.class).where("active", true).get();
  ```

- **`all(Class<R> clazz)`**  
  Fetches all records.
  ```java
  List<User> allUsers = User.all(User.class);
  ```

- **`find(Class<R> clazz, Object id)`**  
  Retrieves a record by ID.
  ```java
  Optional<User> user = User.find(User.class, 42);
  ```

- **`whereIn(Class<R> clazz, String column, Collection<?> values)`**  
  Fetches records where `column` is in `values`.
  ```java
  List<User> users = User.whereIn(User.class, "id", List.of(1, 2, 3));
  ```

- **`findByIdAsync(Class<R> clazz, Object id)`**  
  Async version of `findById`.
  ```java
  CompletableFuture<Optional<User>> futureUser = User.findByIdAsync(User.class, 42);
  ```

#### Instance Methods
- **`save()`**  
  Inserts or updates the record based on whether it's new (ID is `null` or 0).
  ```java
  User user = new User();
  user.name = "Alice";
  user.save(); // INSERT
  user.email = "alice@example.com";
  user.save(); // UPDATE
  ```

- **`delete()`**  
  Deletes the record from the database and cache.
  ```java
  user.delete();
  ```

- **`refresh()`**  
  Reloads the entity's data from the database.
  ```java
  user.refresh();
  ```

---

## QueryBuilder Class

### Building Queries
Chain methods to construct queries:

```java
List<User> users = User.query(User.class)
  .where("age", ">", 18)
  .orderBy("name", "ASC")
  .limit(10)
  .with("posts.comments") // Eager load posts and their comments
  .get();
```

### Methods
- **`where(String column, Object value)`**  
  Adds a condition (default operator `=`).
  ```java
  .where("email", "alice@example.com")
  ```

- **`orderBy(String column, String direction)`**  
  Sorts results by `column` in `ASC`/`DESC` order.
  ```java
  .orderBy("created_at", "DESC")
  ```

- **`limit(int)` / `offset(int)`**  
  Paginates results.
  ```java
  .limit(5).offset(10) // Fetch 5 records starting from the 11th
  ```

- **`with(String... relations)`**  
  Eager loads nested relationships (avoids N+1 queries).
  ```java
  .with("author.profile", "comments")
  ```

---

## Relationships (Example)

### One-to-Many
Define a `Post` model with a `ManyToOne` relationship to `User`:
```java
public class Post extends Model<Post> {
  public String title;
  public String content;

  // ManyToOne: Each post belongs to a user
  public User user;
}
```

**Fetching Related Posts**
```java
List<User> users = User.all(User.class);
users.forEach(user -> {
  List<Post> posts = user.includeManyOf(Post.class); // Uses includeManyOf (experimental)
});
```

---

## Caching
Entities are cached when fetched. Use annotations (e.g., `@Cached`) to enable caching:
```java
@Cached
public class User extends Model<User> { ... }
```
Cached entities are invalidated on `delete()` or `save()`.

---

## Notes
- **Async Operations**: Methods like `findByIdAsync` use `CompletableFuture` for non-blocking IO.
- **Concurrency**: `prefetch` uses parallel streams to fetch multiple entities concurrently.
- **Recursive Generics**: The `Model<M>` pattern ensures method chaining returns the correct subclass type.

---

## Example Usage

### Full Example
```java
// Configure Data Source
HikariDataSource dataSource = ...;
Model.setDataSource(dataSource);

// Define Model
public class User extends Model<User> {
  public String name;
  public String email;
}

// Create and Save User
User user = new User();
user.name = "Bob";
user.email = "bob@example.com";
user.save();

// Query with Conditions
List<User> activeUsers = User.query(User.class)
  .where("active", true)
  .orderBy("name", "ASC")
  .get();

// Fetch with Eager Loading (posts and comments)
List<User> usersWithPosts = User.query(User.class)
  .with("posts.comments")
  .limit(5)
  .get();
``` 

This documentation provides a foundation for using the `Model` and `QueryBuilder` classes. Adjust annotations and relationship handling based on your specific ORM needs.