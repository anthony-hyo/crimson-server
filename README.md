# Crimson Server - MMO RPG Multiplayer Server (Test / Learning)

Crimson Server is an experimental MMO RPG multiplayer server project written in **Java**, utilizing **Netty** for network communication and a **custom-built ORM** created from scratch, documented in the `Bakuretsu.md` file, to manage data persistence.

---

## Project Overview

This project aims to explore and learn the development of multiplayer MMO RPG servers, focusing on:

- Handling multiple simultaneous player connections via Netty
- Implementing game logic for avatars including players, NPCs, and monsters
- Managing persistent game data with a custom ORM layer (package `com.crimson.bakuretsu`)
- The ORM design and implementation details are documented in [`Bakuretsu.md`](src/main/java/com/crimson/bakuretsu/Bakuretsu.md)
- Structured request handling and network protocols

---

## Technologies & Architecture

- **Java 21+** (recommended)
- **Netty** for asynchronous TCP networking (`com.crimson.network` package)
- Custom ORM framework (`com.crimson.bakuretsu`)
  - Includes annotations for entities and relations
  - Caching, query builder, model metadata handling
  - Detailed documentation available in [`Bakuretsu.md`](src/main/java/com/crimson/bakuretsu/Bakuretsu.md)
- Modular design:
  - `avatar` package: Player, NPC, Monster avatars and related data
  - `controller` package: Controllers to process game actions (`PlayerController`)
  - `requests` package: Request/response handling system
  - `config` package: Configuration and game data loading

---

## Project Structure

```
src/main/java/com/crimson/
├── annotations/            # Custom annotations for requests, etc.
├── avatar/                 # Game avatars: players, monsters, NPCs, etc.
│   ├── player/
│   ├── npc/
│   └── monster/
├── bakuretsu/              # Custom ORM implementation
│   ├── Bakuretsu.md        # Documentation of the ORM design and implementation
│   ├── annotations/
│   ├── cache/
│   ├── core/
│   ├── database/
│   ├── enums/
│   ├── models/
│   └── query/
├── config/                 # Configurations and game data
├── controller/             # Controllers for game logic
├── exceptions/             # Custom exceptions
├── interfaces/             # Interfaces for requests and dispatching
├── network/                # Netty network handlers, encoders, decoders
├── requests/               # Request factory and handlers
└── Main.java               # Main server entry point

````

---

## How to Build and Run

1. Clone the repository:

```bash
git clone https://github.com/yourusername/crimson-server.git
cd crimson-server
```

2. Build the project:

```bash
./gradlew build
```

3. Run the server:

 ```bash
java -cp target/crimson-server.jar com.crimson.Main
 ```

4. Connect clients to the server for multiplayer testing (clients are not included in this repo).

---

## Current Features

* Network server handling multiple connections with Netty
* Basic MMO RPG avatar models (Player, NPC, Monster)
* Custom ORM supporting entity annotations, relationships, and caching (unfinished but functional)
* Detailed ORM design explained in [`Bakuretsu.md`](src/main/java/com/crimson/bakuretsu/Bakuretsu.md)
* Area and frame models for game world representation

---

## Future Plans

* Implement full client-server synchronization
* Expand gameplay logic (combat, quests, inventory)
* Further ORM improvements: advanced queries, query caching, testing tools, and migrations
* Implement security and authentication mechanisms

---

## Contributing

This project is open for contributions! Feel free to open issues or submit pull requests. This is primarily a learning and experimental codebase, so any help improving code quality, features, or documentation is appreciated.

---

## License

This project is licensed under the MIT License.

---

## Contact

If you have questions or want to discuss the project, feel free to reach out.
