# Display the this help
help:
    @just --list

# Run tests
test:
    clojure -X:kaocha

# Check and fix formatting with cljstyle; MODE is one of "check" or "fix" (default).
style MODE='fix':
    clojure -M:cljstyle -m cljstyle.main {{MODE}} ./examples ./src ./test