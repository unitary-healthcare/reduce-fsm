# Changelog

## [Unreleased]

- ...

> :raised_hand: **Important:** All the releases **after** 0.1.4 refer to this
> fork and not to the original [cdorrat/reduce-fsm][gh:original] library.

## [1.0.0]

- Migrate from Leiningen to Deps
- Update dependencies
  - core.match 1.0.0
  - dorothy 0.0.7

> :raised_hand: **Important:** All releases below this point refer to the
> original [cdorrat/reduce-fsm][gh:original] library.

## 0.1.4

- Fix Issue #9 - invalid excpetion call breaks Clojure 1.8 compatability.
- Bumped dependencies for clojure -> 1.7.0 & core.match -> 0.2.2

## 0.1.3

- support for specifying the initial state of the fsm at runtime

## 0.1.0

- Added support for incremental finite state machines. These allow you to
  provide events via a function call instead of a sequence.  Useful for when
  events are provided by callbacks
- Updated dependencies:
  - clojure 1.5.1
  - core.match 0.2.0-rc3
  - dorothy 0.0.3
- Fixed a bug that would cause the state machines to overflow the stack on large input sequences

[Unreleased]: https://github.com/unitary-healthcare/reduce-fsm/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/unitary-healthcare/reduce-fsm/compare/v0.1.4...v1.0.0

[gh:original]: https://github.com/cdorrat/reduce-fsm
