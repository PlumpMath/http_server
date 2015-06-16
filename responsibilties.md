# main.clj

- Command line handling
- Start up the server

# server.clj

- Receive input from an input-stream and send responses to
output-stream

# read.clj [OK]

- Read input including CRs and LFs

# adaptor.clj

- Parse input to request maps

# handler.clj [OK but refactor]

- Transform request maps into responses

# files.clj [OK]

- Handle file operations

# operations.clj

- Operations to provide statuses, headers and bodies for responses

# log.clj [OK]

- Handle logging of requests
