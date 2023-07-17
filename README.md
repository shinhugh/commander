# Commander

*This project is under active development.*

A multiplayer turn-based strategy game inspired by Nintendo's classic title
*Advance Wars*.

## Back-end

The back-end is built with the Spring framework (Java).

### HTTP

---

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

5IXhs9tmu3wVIKR1ACrrCbq4piF6xdttVNJIf74I3sdX8JbGNueLf1H7tIjcvs9u
```

Authorization policy: Public

If the client is already logged in, the token received via the request will be
sent back unmodified via the response. This does not extend the lifetime of the
token.

---

**Logout**

```
DELETE /api/auth
```

```
200
```

Authorization policy: Public

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

### WebSocket

*This section will be populated soon.*

### Persistence

Account information is stored via MySQL (MariaDB). Game state data is stored via
MongoDB.

In-memory persistence and caches are not used, as the turn-based nature of the
game does not require frequent updates. Additionally, as a player's turn is not
time-restricted, a game may exist indefinitely; holding all game data in memory
is not a good idea.

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