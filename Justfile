# Display the this help
help:
    @just --list

# Run unit tests for Clojure
test-clj:
    clojure -M:dev:kaocha -m kaocha.runner unit-clj

# Run unit tests for ClojureScript
test-cljs:
    clojure -M:dev:kaocha-cljs -m kaocha.runner unit-cljs

# Check and fix formatting with cljstyle; MODE is one of "check" or "fix" (default).
style MODE='fix':
    clojure -M:cljstyle -m cljstyle.main {{MODE}} ./examples ./src ./test

# Lint the code with clj-kondo
lint:
    clj-kondo --lint ./src ./test ./examples

# Initialize linting with clj-kondo; you should run this once
lint-init:
    clj-kondo --lint "`clojure -A:dev -Spath`" --dependencies --parallel --copy-configs