# Hamcrest Findings

While working on *Hamcrest &mdash; Next Deed* and while working with Hamcrest at CoreMedia I found
some design flaws in Hamcrest. And its not only me, there are also others who wish something would
be different with Hamcrest:

* [Jacob Zimmerman: Redesigning Hamcrest | Java Code Geeks][jzimmerman-redesigning-hamcrest]

    and my answer at CoreMedia Minds blog:
    [Re: Redesigning Hamcrest | Minds][minds-redesigning-hamcrest]

    Jacob for example mentions quirks with the Description class or the conflict between Java 8
    lambdas and matchers.

## The World Changes: Match vs. Mismatch Description

One great advantage Hamcrest provides over for example matching via Java 8 (or Guava) Predicates is
that they are able to describe the expectation and the mismatch quite nicely. But having a look at
`MatcherAssert` (just as [Jacob Zimmerman did][jzimmerman-redesigning-hamcrest]) you will notice
that the match and the generation of the mismatch descriptions are done independently:

```java
public static <T> void assertThat(String reason, T actual, Matcher<? super T> matcher) {
    if (!matcher.matches(actual)) {
        Description description = new StringDescription();
        description.appendText(reason)
                   .appendText("\nExpected: ")
                   .appendDescriptionOf(matcher)
                   .appendText("\n     but: ");
        matcher.describeMismatch(actual, description);

        throw new AssertionError(description.toString());
    }
}
```

The problem especially occurs in integration tests. I always like to take a rich web application as
example which might continuously change its state during the test. Let's assume you want to check
the visibility of an element:

1. `!visibleMatcher.test(webElement)` &mdash; element is not visible, so let's build the mismatch
    description

2. `visibleMatcher.describeMismatch(webElement, description)`

    The matcher now might want to provide some more description on the web element for debugging
    purpose. Let's assume that the element was not visible because it was moved out of the browsers
    visible bounds. Now while generating the mismatch description all attributes of the element are
    added. But, what's that? The attributes clearly tell that the element is visible.

The problem gets worse the more complicated the element under test is. The flaw becomes more obvious
in the `DiagnosingMatcher` which is for example used for `AllOf` matcher:

```java
@Override
public final boolean matches(Object item) {
    return matches(item, Description.NONE);
}

@Override
public final void describeMismatch(Object item, Description mismatchDescription) {
    matches(item, mismatchDescription);
}

protected abstract boolean matches(Object item, Description mismatchDescription);
```

It even creates the mismatch description twice, first on match (redirected to `/dev/null`, if you
know what I mean) and later on during mismatch description. For integration tests it is quite
obvious that the first mismatch description might differ from the second one.

### Safe State &mdash; A Possible Solution?

When I first met this problem I thought it is a good idea to store the state between match and
description mismatch. But only in simple use cases it is clear when you might throw away the state.
It might be obvious that you can throw away the state on match as obviously there is no mismatch...
but that is only true until you meet the `IsNot` matcher. It reverts your expectations. Suddenly
the mismatch is when the matcher actually recognized a match.

So perhaps throw away the state together with the matchers instance. But this only works reliably
if you always create a new matcher for each assertion. You must not reuse it and (obviously) the
matcher must not be a singleton possibly used in multi-threaded test execution.

[minds-redesigning-hamcrest]: <http://minds.coremedia.com/2015/01/22/re-redesigning-hamcrest/> "Re: Redesigning Hamcrest | Minds"
[jzimmerman-redesigning-hamcrest]: <http://www.javacodegeeks.com/2015/01/redesigning-hamcrest.html> "Jacob Zimmerman: Redesigning Hamcrest | Java Code Geeks"
