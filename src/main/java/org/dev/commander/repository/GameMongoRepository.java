package org.dev.commander.repository;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.dev.commander.model.game.*;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class GameMongoRepository implements GameRepository {
    private static final String DATABASE_NAME = "commander";
    private static final String COLLECTION_NAME = "games";
    private final String connectionUri;

    public GameMongoRepository(String connectionUri) {
        this.connectionUri = connectionUri;
    }

    @Override
    public Game readGameById(long id) {
        Document gameDocument;
        try (MongoClient mongoClient = MongoClients.create(connectionUri)) {
            MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
            MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME);
            gameDocument = collection.find(Filters.eq("id", id)).first();
        }
        if (gameDocument == null) {
            throw new NotFoundException();
        }
        return parseGame(gameDocument); // TODO: Handle unexpected exceptions due to malformed data
    }

    @Override
    public Game createGame(Game game) {
        try (MongoClient mongoClient = MongoClients.create(connectionUri)) {
            MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
            MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME);
            Random random = new Random();
            long id = random.nextLong(Long.MAX_VALUE);
            while (collection.find(Filters.eq("id", id)).first() != null) {
                id = random.nextLong(Long.MAX_VALUE);
            }
            Game updatedGame = new Game();
            updatedGame.setId(id);
            updatedGame.setPlayers(game.getPlayers());
            updatedGame.setCurrentTurn(game.getCurrentTurn());
            updatedGame.setWorld(game.getWorld());
            Document gameDocument = serializeGame(updatedGame);
            collection.insertOne(gameDocument);
            return updatedGame;
        }
    }

    @Override
    public Game updateGameById(long id, Game game) {
        try (MongoClient mongoClient = MongoClients.create(connectionUri)) {
            MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
            MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME);
            Document gameDocument = collection.find(Filters.eq("id", id)).first();
            if (gameDocument == null) {
                throw new NotFoundException();
            }
            Game updatedGame = new Game();
            updatedGame.setId(id);
            updatedGame.setPlayers(game.getPlayers());
            updatedGame.setCurrentTurn(game.getCurrentTurn());
            updatedGame.setWorld(game.getWorld());
            Document updatedGameDocument = serializeGame(updatedGame);
            collection.replaceOne(Filters.eq("id", id), updatedGameDocument);
            return updatedGame;
        }
    }

    @Override
    public void deleteGameById(long id) {
        try (MongoClient mongoClient = MongoClients.create(connectionUri)) {
            MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
            MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME);
            if (collection.deleteOne(Filters.eq("id", id)).getDeletedCount() == 0) {
                throw new NotFoundException();
            }
        }
    }

    private Game parseGame(Document document) {
        Game game = new Game();
        long id = document.get("id", Number.class).longValue();
        game.setId(id);
        Map<Integer, Player> playerMap = new HashMap<>();
        Document playersDocument = document.get("players", Document.class);
        for (String key : playersDocument.keySet()) {
            playerMap.put(Integer.parseInt(key), parsePlayer(playersDocument.get(key, Document.class)));
        }
        ArrayList<Integer> playerOrder = document.get("playerOrder", ArrayList.class);
        List<Player> players = new ArrayList<>();
        for (int playerId : playerOrder) {
            players.add(playerMap.get(playerId));
        }
        game.setPlayers(players);
        int currentTurn = document.get("currentTurn", Integer.class);
        game.setCurrentTurn(currentTurn);
        World world = parseWorld(document.get("world", Document.class), playerMap);
        game.setWorld(world);
        return game;
    }

    private Player parsePlayer(Document document) {
        Player player = new Player();
        player.setAccountId(document.get("accountId", Number.class).longValue());
        return player;
    }

    private World parseWorld(Document document, Map<Integer, Player> playerMap) {
        World world = new World();
        ArrayList<ArrayList<Document>> boardSpotDocumentList = document.get("board", ArrayList.class);
        BoardSpot[][] board = new BoardSpot[boardSpotDocumentList.size()][boardSpotDocumentList.get(0).size()];
        world.setBoard(board);
        for (int row = 0; row < boardSpotDocumentList.size(); row++) {
            for (int col = 0; col < boardSpotDocumentList.get(row).size(); col++) {
                board[row][col] = parseBoardSpot(boardSpotDocumentList.get(row).get(col), playerMap);
            }
        }
        return world;
    }

    private BoardSpot parseBoardSpot(Document document, Map<Integer, Player> playerMap) {
        BoardSpot boardSpot = new BoardSpot();
        Document terrainDocument = document.get("terrain", Document.class);
        if (terrainDocument != null) {
            boardSpot.setTerrain(parseTerrain(terrainDocument, playerMap));
        }
        Document unitDocument = document.get("unit", Document.class);
        if (unitDocument != null) {
            boardSpot.setUnit(parseUnit(unitDocument, playerMap));
        }
        return boardSpot;
    }

    private Terrain parseTerrain(Document document, Map<Integer, Player> playerMap) {
        Terrain terrain = new Terrain();
        Terrain.Type type = switch (document.get("type", String.class)) {
            case "meadow" -> Terrain.Type.MEADOW;
            case "forest" -> Terrain.Type.FOREST;
            case "mountain" -> Terrain.Type.MOUNTAIN;
            case "road" -> Terrain.Type.ROAD;
            case "sea" -> Terrain.Type.SEA;
            case "hq" -> Terrain.Type.HQ;
            case "city" -> Terrain.Type.CITY;
            case "barracks" -> Terrain.Type.BARRACKS;
            case "factory" -> Terrain.Type.FACTORY;
            case "shipyard" -> Terrain.Type.SHIPYARD;
            case "airbase" -> Terrain.Type.AIRBASE;
            default -> throw new RuntimeException("Unrecognized terrain type");
        };
        terrain.setType(type);
        Integer playerIdWrapper = document.get("playerId", Integer.class);
        if (playerIdWrapper != null) {
            terrain.setOwner(playerMap.get(playerIdWrapper));
        }
        Integer hpWrapper = document.get("hp", Integer.class);
        if (hpWrapper != null) {
            terrain.setHp(hpWrapper);
        }
        Boolean availableWrapper = document.get("available", Boolean.class);
        if (availableWrapper != null) {
            terrain.setAvailable(availableWrapper);
        }
        return terrain;
    }

    private Unit parseUnit(Document document, Map<Integer, Player> playerMap) {
        Unit unit = new Unit();
        Unit.Type type = switch (document.get("type", String.class)) {
            case "grunt" -> Unit.Type.GRUNT;
            case "groundCarrier" -> Unit.Type.GROUND_CARRIER;
            case "recon" -> Unit.Type.RECON;
            case "tank" -> Unit.Type.TANK;
            case "aaMissileLauncher" -> Unit.Type.AA_MISSILE_LAUNCHER;
            case "rocketLauncher" -> Unit.Type.ROCKET_LAUNCHER;
            case "seaCarrier" -> Unit.Type.SEA_CARRIER;
            case "cruiser" -> Unit.Type.CRUISER;
            case "battleship" -> Unit.Type.BATTLESHIP;
            case "submarine" -> Unit.Type.SUBMARINE;
            case "airCarrier" -> Unit.Type.AIR_CARRIER;
            case "attackHelicopter" -> Unit.Type.ATTACK_HELICOPTER;
            case "jet" -> Unit.Type.JET;
            case "bomber" -> Unit.Type.BOMBER;
            default -> throw new RuntimeException("Unrecognized unit type");
        };
        unit.setType(type);
        Integer playerIdWrapper = document.get("playerId", Integer.class);
        if (playerIdWrapper != null) {
            unit.setOwner(playerMap.get(playerIdWrapper));
        }
        Integer hpWrapper = document.get("hp", Integer.class);
        if (hpWrapper != null) {
            unit.setHp(hpWrapper);
        }
        Boolean availableWrapper = document.get("available", Boolean.class);
        if (availableWrapper != null) {
            unit.setAvailable(availableWrapper);
        }
        return unit;
    }

    private Document serializeGame(Game game) {
        Document gameDocument = new Document();
        gameDocument.put("id", game.getId());
        Document playersDocument = new Document();
        gameDocument.put("players", playersDocument);
        Map<Player, Integer> playerMap = new HashMap<>();
        List<Integer> playerOrder = new ArrayList<>();
        gameDocument.put("playerOrder", playerOrder);
        for (int i = 0; i < game.getPlayers().size(); i++) {
            Player player = game.getPlayers().get(i);
            playerMap.put(player, i);
            playersDocument.put(String.valueOf(i), serializePlayer(player));
            playerOrder.add(i);
        }
        gameDocument.put("currentTurn", game.getCurrentTurn());
        gameDocument.put("world", serializeWorld(game.getWorld(), playerMap));
        return gameDocument;
    }

    private Document serializePlayer(Player player) {
        Document playerDocument = new Document();
        playerDocument.put("accountId", player.getAccountId());
        return playerDocument;
    }

    private Document serializeWorld(World world, Map<Player, Integer> playerMap) {
        Document worldDocument = new Document();
        ArrayList<ArrayList<Document>> boardSpotDocumentList = new ArrayList<>();
        worldDocument.put("board", boardSpotDocumentList);
        for (BoardSpot[] row : world.getBoard()) {
            ArrayList<Document> boardSpotDocumentRowList = new ArrayList<>();
            boardSpotDocumentList.add(boardSpotDocumentRowList);
            for (BoardSpot boardSpot : row) {
                boardSpotDocumentRowList.add(serializeBoardSpot(boardSpot, playerMap));
            }
        }
        return worldDocument;
    }

    private Document serializeBoardSpot(BoardSpot boardSpot, Map<Player, Integer> playerMap) {
        Document boardSpotDocument = new Document();
        if (boardSpot.getTerrain() != null) {
            boardSpotDocument.put("terrain", serializeTerrain(boardSpot.getTerrain(), playerMap));
        }
        if (boardSpot.getUnit() != null) {
            boardSpotDocument.put("unit", serializeUnit(boardSpot.getUnit(), playerMap));
        }
        return boardSpotDocument;
    }

    private Document serializeTerrain(Terrain terrain, Map<Player, Integer> playerMap) {
        Document terrainDocument = new Document();
        String type = switch (terrain.getType()) {
            case MEADOW -> "meadow";
            case FOREST -> "forest";
            case MOUNTAIN -> "mountain";
            case ROAD -> "road";
            case SEA -> "sea";
            case HQ -> "hq";
            case CITY -> "city";
            case BARRACKS -> "barracks";
            case FACTORY -> "factory";
            case SHIPYARD -> "shipyard";
            case AIRBASE -> "airbase";
        };
        terrainDocument.put("type", type);
        terrainDocument.put("playerId", playerMap.get(terrain.getOwner()));
        terrainDocument.put("hp", terrain.getHp());
        terrainDocument.put("available", terrain.isAvailable());
        return terrainDocument;
    }

    private Document serializeUnit(Unit unit, Map<Player, Integer> playerMap) {
        Document terrainDocument = new Document();
        String type = switch (unit.getType()) {
            case GRUNT -> "grunt";
            case GROUND_CARRIER -> "groundCarrier";
            case RECON -> "recon";
            case TANK -> "tank";
            case AA_MISSILE_LAUNCHER -> "aaMissileLauncher";
            case ROCKET_LAUNCHER -> "rocketLauncher";
            case SEA_CARRIER -> "seaCarrier";
            case CRUISER -> "cruiser";
            case BATTLESHIP -> "battleship";
            case SUBMARINE -> "submarine";
            case AIR_CARRIER -> "airCarrier";
            case ATTACK_HELICOPTER -> "attackHelicopter";
            case JET -> "jet";
            case BOMBER -> "bomber";
        };
        terrainDocument.put("type", type);
        terrainDocument.put("playerId", playerMap.get(unit.getOwner()));
        terrainDocument.put("hp", unit.getHp());
        terrainDocument.put("available", unit.isAvailable());
        return terrainDocument;
    }
}
