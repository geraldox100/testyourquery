TestYourQuery is a JUnit integrated framework for unit testing your querys.
==

I am unit testing defender. Sometimes TDD and sometimes not. I believe that without automated tests, maintainability becomes a very difficult task. For unit testing, my favorite tools are JUnit, Mockito and Hamcrest.

But sometimes its simply not enough. In the case of queries, if we create mocks to simulate a database, we are not realy testing our queries.

I looked for tools that would allow me to easily simulate a database, and even found a few, but none with the simplicity that I wanted.

So I decided to create an integrated framework to JUnit that would allow me to easily create a database, create test mass, run my queries and test the results.

Take a look on <a href="https://github.com/geraldox100/testyourquery-test">this</a> project for usage examples.

Usage
--

Maven dependency

To use module on Maven-based projects, use following dependency:

````
<dependency>
	<groupId>br.com.geraldoferraz</groupId>
	<artifactId>testyourquery</artifactId>
	<version>1.0.0</version>
</dependency>
````
(or whatever version is most up-to-date at the moment)


Getting Started
--
Basicly, what you need to do is use the Runner implementation TestYourQueryRunner

````
@RunWith(TestYourQueryRunner.class)
class PersonDaoTest{
	
}
````

Since the goal is to test queries, TestYourQuery will automatically create an in memory database. Also, it will seek annotated classes with @Entity annotation to create the database structure. To create a mass of data, simply declare an EntityManager or Connection annotated with PersistenceContext and JDBCConnection respectively.

````
@RunWith(TestYourQueryRunner.class)
class PersonDaoTest{
	@PersistenceContext
	private EntityManager em;
	
	@JDBCConnection
	private Connection conn;
	
	@Before
	public void before() {
		Person c1 = new Person("Geraldo");
		Person c2 = new Person("Michel");
		Person c3 = new Person("Gabriela");
		Person c4 = new Person("Agnelito");

		em.persist(c1);
		em.persist(c2);
		em.persist(c3);
		em.persist(c4);
		
	}
}
````
The EntityManager is just a helper, what we really want is test our DAO classes. Just add your DAO and put the @DAO annotation on it
````
@Dao
private PersonDao dao;
````
TestYourQuery will find on your class an EntityManager field and inject a new instance on it. Never mind instantiating your DAO, TestYourQuery will do that as well :)

Now to the fun part, Test Your Query!
```
@Test
public void searchByANameThatExists() {
	Person geraldo = dao.searchByName("Geraldo");

	assertNotNull(geraldo);
	assertEquals(geraldo.getNome(), "Geraldo");
}

@Test
public void searchByPartOfANameThatExists() {
	Person geraldo = dao.searchByName("ich");

	assertNotNull(geraldo);
	assertEquals(geraldo.getNome(), "Michel");
}

@Test(expected = Exception.class)
public void searchByANameThatDontExist() {
	dao.searchByName("sbrables");
}
```

Custom Configuration
--
If for some reason you have a large classpath and you want to specify the entities location, just implement a static method annotated with Configurator that returns a Configuration instance. To create a Configuration instance, use the ConfigurationFactory.
```
@Configurator
public static Configuration configuration() {
	return new ConfigurationFactory()
		.searchEntitiesAt("br.com.geraldoferraz")
		.build();
}
```
Or if you prefer, add your classes one by one
```
@Configurator
public static Configuration configuration() {
	return new ConfigurationFactory()
		.addEntity(Person.class)
		.build();
}
```
For performance reasons, TestYourQuery creates the database only once for the whole test class. If you want it to re-create the database for each test, just use add it to the configuration.

```
@Configurator
public static Configuration configuration() {
	return new ConfigurationFactory()
		.searchEntitiesAt("br.com.geraldoferraz")
		.withSessionPerTestMode()
		.build();
}
```
If you dont want an in memroy database or simply prefer to use your own database configuration, just use the persistence-unit you want.

```
@Configurator
public static Configuration configuration() {
	return new ConfigurationFactory()
		.searchEntitiesAt("br.com.geraldoferraz")
		.withSessionPerTestMode()
		.persistenceUnit("geraldoferraz")
		.build();
}
```
TestYourQuery will search your classpath for a persistence-unit that matches.

The Configuration class is very simple, it has only two fields, SessionMode and EntityManagerProvider.
The SessionMode indicates if you want a single database for the whole class or to re-create it for each test.
The EntityManagerProvider interface is the provider to the database access. You can create your own implementation and pass it to the ConfigurationFactory.

```
@Configurator
public static Configuration configuration() {
	return new ConfigurationFactory()
		.searchEntitiesAt("br.com.geraldoferraz")
		.withSessionPerTestMode()
		.withProvider(new MyProvider())
		.build();
}

public class MyProvider implements EntityManagerProvider {

	public EntityManagerFactory getEntityManagerFactory() {
		return Persistence.createEntityManagerFactory("geraldoferraz");
	}

}
```
