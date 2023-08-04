# Pixels

*This project is under active development. The game has shifted quite a bit
from its original concept and will likely continue to do so.*

A real-time social multiplayer game, designed and built from the ground up.

Screenshot (2023-08-03):
![Screenshot](dev/screenshot.png)

## Back-end

The back-end is built with the Spring framework (Java).

### HTTP API

**Login via username and password**

```
POST /api/auth

{
  "username": "somebody",
  "password": "somesecret"
}
```

```
200

O2vIi4gkS1fDMDCM5IwGX2CM4UvFeFoVB5QTQ5k74tOE0YMIyNBbrW09GUAXTHnp0YKvGk3OiXuky5AkFv8sjdU5bgsxNSddiHQIcnHsrkx1aHJ8AzLhGetLBGB6BPri
```

Authorization policy: Public

If the client is already logged in, the token received via the request will be
sent back unmodified via the response. This does not extend the lifetime of the
token.

The entirety of the response body is the token that should be sent in subsequent
requests as the bearer token (authorization header).

---

**Logout**

```
DELETE /api/auth
```

```
200
```

Authorization policy: Public

The server will unconditionally respond with status code 200.

---

**Fetch account information**

```
GET /api/account?id=8
```

```
200

{
  "id": 8,
  "loginName": "somebody",
  "authorities": 1,
  "publicName": "Some person"
}
```

Authorization policy: Public (with filtering)

The `loginName` and `authorities` fields are included only if the requesting
client is either logged in as the requested account or an admin.

---

**Create account**

```
POST /api/account

{
  "loginName": "somebody",
  "password": "somesecret",
  "publicName": "Some person"
}
```

```
200

{
  "id": 8,
  "loginName": "somebody",
  "authorities": 1,
  "publicName": "Some person"
}
```

Authorization policy: Public

---

**Modify account**

```
PUT /api/account?id=8

{
  "loginName": "somebody",
  "password": "somesecret",
  "publicName": "Some person"
}
```

```
200

{
  "id": 8,
  "loginName": "somebody",
  "authorities": 1,
  "publicName": "Some person"
}
```

Authorization policy: Owner or admin

Only an admin can set the `authorities` field.

---

**Delete account**

```
DELETE /api/account?id=8
```

```
200
```

Authorization policy: Owner or admin

---

*More entries will be added here soon.*

### WebSocket API

*This section will be populated soon.*

### Persistence

Account information is stored via MySQL (MariaDB). Game state data is stored via
MongoDB.

In-memory persistence and caches are not used, as the turn-based nature of the
game does not require frequent updates. Additionally, as a player's turn is not
time-restricted, a game may exist indefinitely; holding all game data in memory
is not a good idea.

## Front-end

*This section will be populated soon.*

## Development status

The groundwork for the game has been complete.

- Systems are in place for authentication (custom bearer tokens and cookies),
account management, and friends (REST API).
- Bi-directional communication is possible between the server and client, and
both sides are able to send and receive game state updates.
- Collision detection is implemented on both the server and client. The client
generates new positions, and the server verifies every update to enforce game
integrity.
- Adjustable values such character travel error margins and update frequencies
are still being ironed out as more testing happens.

As ideas for the game get fleshed out, more game-specific logic will be added.