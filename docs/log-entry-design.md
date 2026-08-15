## LogEntry

`LogEntry` represents the value of one parsed log event.

### Representation

I will use a Java `record`

Reasons:

- `LogEntry` currently has **value semantics**.
- Its state is completely described by its components.
- There is no independent domain identity.
- Two `LogEntry` instances containing the same component values should be logically equal
- Its state should not change after construction.
- A record expresses these semantics directly with less implementation boilerplate.

-----

### Fields

#### `timestamp: Instant`

Represents the absolute point in time when the event occured.

I prefer `Instant` over `String` because the value has temporal semantics rather than textual semantics.

I prefer `Instant` over `LocalDateTime` because the log timestamp represents an absolute point on the timeline rather
than a local wall-clock time.

#### `level: LogLevel`

Supported values:
`TRACE`, `DEBUG`, `INFO`, `WARN`, `ERROR`

The domain contains a finite set of valid values, so an enum restricts the representable state space and prevents
arbitrary level strings.

#### `service: String`

A `String` is sufficient for the current requirements.

A dedicated `ServiceName` value object may become useful later if service names acquire additional validation or domain
behavior.

#### `message: String`

Stores the event message.

An empty message is valid, but `null` is not.

-----

### Invariants

For every valid `LogEntry`:

- `timestamp != null`
- `level != null`
- `service != null`
- `!service.isBlank()`
- `message != null`

------

### Non-goal

Current `LogEntry` does not handle:

- raw log parsing
- normalization
- persistence
- event identity
- service registry validation
- arbitrary log levels
