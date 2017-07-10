Fuzzy is a handy little library for writing expressive
["fuzz tests"](https://en.wikipedia.org/wiki/Fuzz_testing) in Java.

[![Build Status](https://travis-ci.org/redfin/fuzzy.svg?branch=master)](https://travis-ci.org/redfin/fuzzy)
[![Coverage Status](https://coveralls.io/repos/github/redfin/fuzzy/badge.svg)](https://coveralls.io/github/redfin/fuzzy)
[![fuzzy-core Javadoc](http://javadoc-badge.appspot.com/com.redfin/fuzzy-core.svg?label=fuzzy-core+javadoc)](http://javadoc-badge.appspot.com/com.redfin/fuzzy-core)
[![fuzzy-core Javadoc](http://javadoc-badge.appspot.com/com.redfin/fuzzy-junit-4.svg?label=fuzzy-junit-4+javadoc)](http://javadoc-badge.appspot.com/com.redfin/fuzzy-junit-4)

```java
public void test() {
    // Create an input that can be one of three different strings
    Generator<String> myString = Generator.of("A", "B", "C");

    // The value of myString returned by the .get() method will be
    // different for each iteration of the test.
    System.out.println(myString.get());

    // You can use the generator multiple times during your test, and
    // each time it will return the same value.
    assert myString.get() != "D";
}
```

Fuzzy also comes with some built-in generators that ensure that your
code is tested against common input edge cases. For example, a generator
of `Any.integer()` will inject your inputs with negative values,
positive values, and zero.

* [Installation](#user-content-installation)
* [Use with JUnit](#user-content-use-with-junit)
* [Generators and Cases](#user-content-generators-and-cases)
* [Behavioral Specifications](#user-content-behavioral-specifications)
* [Permutation Modes](#user-content-permutation-modes)
* [Use with Other and Custom Test Frameworks](#user-content-use-with-other-and-custom-test-frameworks)
* [FAQ](#user-content-faq)
* [`FuzzyRule` Options](#user-content-fuzzyrule-options)
* [Contributing](#user-content-contributing)

# Installation

At a minimum, you will need to take a dependency on the
[`fuzzy-core`](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.redfin%22%20a%3A%22fuzzy-core%22)
library:

```xml
<!-- Maven -->
<dependency>
    <groupId>com.redfin</groupId>
    <artifactId>fuzzy-core</artifactId>
    <version>0.7-SNAPSHOT</version>
</dependency>
```

In addition, if you're using a supported unit testing framework 
(currently only Junit 4), you can take a dependency on the library
for that framework:

```xml
<!-- Maven Junit 4 -->
<dependency>
    <groupId>com.redfin</groupId>
    <artifactId>fuzzy-junit-4</artifactId>
    <version>0.7-SNAPSHOT</version>
</dependency>
```

# Use with JUnit

The `FuzzyRule` test rule can be added to your JUnit test cases to
automatically execute your tests against various permutations of their
input variables:

```java
public class MyClassTest {

    @Rule public FuzzyRule fuzzyRule = FuzzyRule.DEFAULT;

    @Test
    public void testSomething() {
        // Declare your test variables
        Generator<String> myString = Generator.of(Any.string());

        // Execute the test
        MyClass subject = new MyClass();

        boolean actual = subject.something(myString.get());
        assertTrue(actual);
    }

}
```

See the [FuzzyRule Options](#user-content-fuzzyrule-options) section
later in this document for a description of the different configuration
settings available.

# Generators and Cases

When writing tests with Fuzzy, you use *Cases* to describe test values,
and *Generators* to declare, retrieve, and use those values.

Each *Case* broadly describes a particular variable. For example, you
might have an `EmailCase implements Case<String>` that describes email
addresses. Cases then provide *subcases* (or edge cases) that cover the
variants of those variables. The `EmailCase` might return subcases for
typical address (e.g., `name@place.com`), the newer top-level domains
(e.g., `jon@itza.pizza`), for special formatting (e.g., `me@[1.1.1.1]`),
and for the more obscure types (e.g., `this."is\ valid"@example.com`).

When you declare a generator for the `EmailCase` in one of your tests,
Fuzzy will make sure to run the test enough times so that each of the
subcases is returned at least once. That's four tests (four subcases)
for the price of one!

Fuzzy will automatically detect all of the generators declared by your
test and use [*pair-wise combination*](#user-content-pairwise-testing)
to ensure that each variable's subcases are all executed. The catch is
that for this to work, you need to declare all variables before you use
any of them, and they all need to be declared on the thread that is
executing your tests. _(Note: it's OK to **access** generators from
another thread, as long as they're all **declared** on the thread
running the tests.)_

**Good:**

```java
@Test
public void myTest() {
   // All of your generators must be declared before any of them are used, so
   // that fuzzy understands the number of necessary test permutations.

   // Declare test variables
   Generator<String> to = Generator.of(new EmailCase());
   Generator<Integer> orderCount = Generator.of(Any.integer().inRange(1, 100));

   // Use your test variables
   assertTrue(subject.sendEmail(to.get(), orderCount.get()));
}
```

**Bad:**

```java
@Test
public void myTest() {
   // All of your generators must be declared before any of them are used, so
   // that fuzzy understands the number of necessary test permutations.

   // Declare and access one generator
   Generator<String> emailGenerator = Generator.of(new EmailCase());
   String toAddress = emailGenerator.get();

   // Declare and access another generator; you'll get an IllegalStateException
   // on the next line
   Generator<Integer> intGenerator = Generator.of(Any.integer().inRange(1, 100));
   int orderCount = intGenerator.get();
}
```

# Behavioral Specifications

# Permuation Modes

## Pairwise

## Each Subcase At Least Once

# Use with Other and Custom Test Frameworks

For other frameworks or test scenarios, you can configure the test
process manually. Use the `Context` class to initialize and iterate
over the possible permutations.

```java
public void executeFuzzyTest() {
    // Initialize the testing context. This applies to all generators
    // created on this thread.
    Context.init();

    try {
        do {
            // Execute your test code here.
            executeSingleTestIteration();
        } while(Context.next());
    }
    catch(AssertionError | Exception e) {
        // Handle test failures
        // You can use Context.report() or Context.reportTo(StringBuilder)
        // to get a sense of the inputs that caused the test failure.
    }
    finally {
        // Clean up for the next test
        Context.cleanUp();
    }
}
```

# FAQ

# `FuzzyRule` Options

FuzzyRule defines a number of options for controlling the flow of your
tests:

## `failImmediately`

```java
@Rule FuzzyRule fuzzyRule = FuzzyRule.custom()
                                     .withFailImmediately(false)
                                     .build();

@Rule FuzzyRule fuzzyRule = FuzzyRule.REPORTING_ALL_FAILURES;
```

Determines if a failure of any test case iteration will be reported
immediately. When `false`, FuzzyRule will execute your test case against
all expected iterations regardless of whether any individual iteration
results in a test failure. Only the last encountered failure will be
reported to JUnit.

The `FuzzyRule.REPORTING_ALL_FAILURES` preset sets `failImmediately` to
false and also uses a test reporter that summarizes the generated input
values for each failed iteration, which can be helpful when debugging
broken tests.

The default value for `failImmediately` is `true`, meaning that failures
may preempt some test case iterations from executing.

## `testReporter`

```java
@Rule FuzzyRule fuzzyRule = FuzzyRule.custom()
                                     .withTestReporter(...)
                                     .build();

@Rule FuzzyRule fuzzyRule = FuzzyRule.SUMMARIZING;
@Rule FuzzyRule fuzzyRule = FuzzyRule.VERBOSE;
```

Configures the amount of output generated by fuzzy during test runs, or
allows you to attach your own listeners to the testing engine.

The `FuzzyRule.SUMMARIZING` preset will output the number of iterations
executed for each of your test cases.

The `FuzzyRule.VERBOSE` preset will output detailed information during
the test run, including the generated inputs for each test case
iteration.

For custom test reporters, see the documentation for `TestReporter`.

The default value for `testReporter` is `TestReporter.DEFAULT`, which
produces no additional output.

## `maxIterations`

```java
@Rule FuzzyRule fuzzyRule = FuzzyRule.custom()
                                     .withMaxIterations(100)
                                     .build();
```

The maximum number of times each test in your suite will be executed. If
the number of permutations necessary to cover all input pairs in the
test is greater than this bound, they will not be executed.

The default value for `maxIterations` is `100`.

## `failAfterMaxIterations`

```
@Rule FuzzyRule fuzzyRule = FuzzyRule.custom()
                                     .withFailAfterMaxIterations(true)
                                     .build();
```

Determines if tests fail when their input permutations cannot be covered
with fewer than the maximum iterations.

The default value for `failAfterMaxIterations` is `false`.

# Contributing

`TODO: flesh this section out`

* Do *not* add any new runtime dependencies to `fuzzy-core`;
  `fuzzy-junit` should only depend on `junit` itself. In order to make
  it easy to import fuzzy, we don't want to introduce any dependency
  conflicts on our consumers. This is the reason for classes such as
  `FuzzyPreconditions`.

