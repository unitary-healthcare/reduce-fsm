# Display the this help
help:
    @just --list

# Run tests
test:
    clojure -X:kaocha
