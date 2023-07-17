# Commander

*This project is under active development.*

A multiplayer turn-based strategy game inspired by Nintendo's classic title
*Advance Wars*.

## Back-end

The back-end is built with the Spring framework (Java).

### APIs

This section will be created soon.

### Persistence

Account information is stored via MySQL (MariaDB). Game state data is stored via
MongoDB.

In-memory persistence and caches are not used, as the turn-based nature of the
game does not require frequent updates.

## Front-end

There is no client yet, but the plan is to target web browsers. It will be a
single-page app.

## Development status

The back-end (server-side) is currently under development. The front-end
(client-side) will be developed once the back-end is more or less complete.

- Authentication (custom bearer tokens) and account management (REST API) are
fully implemented.
- The WebSocket protocol is currently being integrated into a temporary test
controller to serve as a proof of concept for real-time game state updates.