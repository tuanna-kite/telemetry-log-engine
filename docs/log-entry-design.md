# LogEntry

`LogEntry` represents the value of one parsed log event.

## Representation

I use a Java `record` because:

- `LogEntry` has value semantics.
- Its state is completely described by its components.
- There is currently no independent domain identity.
- Two instances with equal component values represent the same value.
- Its state should not change after construction.
- A record expresses these semantics with minimal boilerplate.

A record does not guarantee deep immutability for mutable component
objects. The current component types are suitable for an effectively
immutable value.

## Fields

### `timestamp: Instant`

Represents the absolute point in time when the event occurred.

`Instant` is preferred over `String` because the value has temporal
semantics rather than textual semantics.

It is preferred over `LocalDateTime` because the event represents an
absolute point on the timeline rather than a local wall-clock time.

### `level: LogLevel`

Supported values:

`TRACE`, `DEBUG`, `INFO`, `WARN`, `ERROR`

The domain has a finite set of valid values, so an enum restricts the
representable state space and prevents arbitrary values.

### `serviceName: String`

A `String` is sufficient for the current requirements.

A dedicated `ServiceName` value object may become useful if service
names later acquire additional validation or domain behavior.

### `message: String`

Stores the event message.

An empty string is valid, but `null` is not.

## Invariants

If a `LogEntry` exists:

- `timestamp != null`
- `level != null`
- `serviceName != null`
- `!serviceName.isBlank()`
- `message != null`

## Validation Strategy

Required null references are rejected with `NullPointerException`.

A non-null but blank `serviceName` is rejected with
`IllegalArgumentException`.

`LogEntry` does not normalize input.

## Equality Semantics

Two instances with equal record components represent the same value.

Equal instances must therefore also produce equal `hashCode()` values.

## Boundary Responsibility

`LogEntry` enforces intrinsic domain invariants.

Raw parsing, textual timestamp conversion, log-level parsing, and input
normalization belong to the parser/boundary layer.

## Non-Goals

Current `LogEntry` does not handle:

- raw log parsing;
- normalization;
- persistence;
- event identity;
- service registry validation;
- dynamic or user-defined log levels.

## Tests Covered

- Valid values preserve all components.
- Empty messages are accepted.
- Null required components are rejected.
- Blank service names are rejected.
- Equal component values produce equal entries and equal hash codes.
- Different component values produce unequal entries.