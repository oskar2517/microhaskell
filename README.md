# Micro Haskell

Micro Haskell is an interpreter for a small subset of the Haskell programming language. It is designed around an intermediate representation rooted in the principles of the lambda calculus. This project serves both as a practical tool for experimenting with functional programming constructs and as an educational platform for studying the theoretical concepts of Haskell and functional languages more broadly.

For more details, please refer to the [report](/MicroHaskell%20Report.pdf) I wrote about the development of this language.

## Features

- Lazy evaluation
- Untyped
- Function definitions
- Function applications
- Anonymous functions (Lambdas)
- Lists (Church-encoded)
- Currying
- Recursive binding
- Basic arithmetic and conditionals
- Let bindings
- Custom operators
- REPL with syntax highlighting

## Example

The following example demonstrates recursive function definitions, arithmetic, currying, and higher-order functions in Micro Haskell:

```haskell
-- Calculates the factorial of n
factorial n = if n == 0 then 1 else n * factorial (n - 1);

-- Calculates the n-th fibonacci number
fibonacci n =
    if n == 0 then 0
    else if n == 1 then 1
    else fibonacci (n - 1) + fibonacci (n - 2);

-- Calculates the great common divisor of a and b
gcd a b =
    if b == 0
    then abs a
    else gcd b (mod a b);

-- Partially applies gcd to 36 (currying)
curriedGcd = gcd 36;

constant = 42;

-- Applies op to arguments a and b
apply op a b = op a b;

-- Calculates whether five is an odd number. Uses let bindings.
isFiveOdd =
    let
        isOdd n =
            if n == 0 then 0
            else isEven (n - 1);

        isEven n =
            if n == 0 then 1
            else isOdd (n - 1);

        n = 5
    in
        isOdd n;

lambda = (\x y -> x * y) 3 4;

main = (apply (+) (curriedGcd 317523) (fibonacci 10)) * factorial 4 - constant + isFiveOdd * lambda;
```

For more examples, checkout the [examples](examples) directory and the Micro Haskell [prelude](src/main/resources/prelude.mhs).

## Future Work

Potential future enhancements to Micro Haskell include:

- Pattern matching
- User-defined algebraic data types
- Improved error handling
