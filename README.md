# Micro Haskell

Micro Haskell is an interpreter for a small subset of the Haskell programming language. It is designed around an immediate representation rooted in the principles of the lambda calculus. This project serves both as a practical tool for experimenting with functional programming constructs and as an educational platform for studying the theoretical concepts of Haskell and functional languages more broadly.

## Features

- Lazy evaluation
- Dynamic typing
- Function definitions
- Function applications
- Currying
- Recursive binding
- Basic arithmetic and conditionals
- Let bindings

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

-- Calculates the modulo of a and b
mod a m =
    if (a - m * (a / m)) < 0
    then (a - m * (a / m)) + (if m < 0 then (0 - m) else m)
    else a - m * (a / m);

-- Returns the absolute value of x
abs x = if x < 0 then (0 - x) else x;

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
            else if n == 1 then 1
            else if n > 1 then isOdd (n - 2)
            else isOdd (n + 2);

        n = 5
    in
        isOdd n;

main = (apply (+) (curriedGcd 317523) (fibonacci 10)) * factorial 4 - constant + isFiveOdd;
```

## Future Work

Potential future enhancements to Micro Haskell include:

- Pattern matching
- User-defined algebraic data types
- REPL interface
- Improved error handling