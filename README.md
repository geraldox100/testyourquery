TestYourQuery is a JUnit integrated framework for unit testing your querys.
==

I am unit testing defender. Sometimes TDD and sometimes not. I believe that without the automated tests maintainability of a system becomes really difficult. For this task, my favorite tools are JUnit, Mockito and Hamcrest.

But sometimes it is simply not enough. In the case of queries, if we create mocks to simulate a database, we are not testing our truth queries.

I looked for tools that would allow me to easily simulate a database, and even found a few, but none with the simplicity that I wanted.

So I decided to create an integrated framework to JUnit that would allow me to easily create a database, create test mass, run my queries and test your results.

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

