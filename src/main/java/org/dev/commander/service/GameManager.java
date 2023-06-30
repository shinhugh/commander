package org.dev.commander.service;

import org.dev.commander.model.Account;
import org.dev.commander.model.game.*;
import org.dev.commander.repository.GameRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class GameManager implements GameService {
    private final AuthorityVerificationService authorityVerificationService;
    private final GameRepository gameRepository;

    public GameManager(AuthorityVerificationService authorityVerificationService, GameRepository gameRepository) {
        this.authorityVerificationService = authorityVerificationService;
        this.gameRepository = gameRepository;
    }

    public void test(Authentication authentication) {
        System.out.println("@@ GameManager.test() invoked");
        if (authentication == null) {
            throw new UnauthorizedException();
        }
        if (!authorityVerificationService.verifyAuthenticationContainsAtLeastOneAuthority(authentication, Set.of("ADMIN"))) {
            throw new ForbiddenException();
        }
    }

    public Game createGame(Authentication authentication, List<Account> accounts) {
        List<Player> players = new ArrayList<>();
        for (Account account : accounts) {
            Player player = new Player();
            player.setAccountId(account.getId());
            players.add(player);
        }
        Game game = new Game();
        game.setPlayers(players);
        game.setCurrentTurn(0);
        game.setWorld(generateMockWorld(players));
        return gameRepository.createGame(game);
    }

    public void leaveGame(Authentication authentication) {
        // TODO
    }

    public void makeMove(Authentication authentication, long gameId, Move move) {
        // TODO: Identify Player
        // TODO: Throw exception if unable to identify Player
        Game game;
        try {
            game = gameRepository.readGameById(gameId);
        }
        catch (NotFoundException ex) {
            throw new GameNotFoundException();
        }
        int originY = move.getOriginY();
        int originX = move.getOriginX();
        Unit.Type create = move.getCreate();
        List<Move.Direction> travel = move.getTravel();
        int attackY = move.getAttackY();
        int attackX = move.getAttackX();
        Move.Direction embark = move.getEmbark();
        boolean capture = move.isCapture();
        boolean submerge = move.isSubmerge();
        BoardSpot[][] board = game.getWorld().getBoard();
        if (originY < 0 || originY >= board.length) {
            throw new InvalidMoveException();
        }
        if (originX < 0 || originX >= board[originY].length) {
            throw new InvalidMoveException();
        }
        boolean terrainMove = create != null;
        boolean unitMove = travel != null || attackY != 0 || attackX != 0 || embark != null || capture || submerge;
        if (terrainMove && unitMove) {
            throw new InvalidMoveException();
        }
        if (unitMove) {
            Unit unit = board[originY][originX].getUnit();
            if (unit == null) {
                throw new InvalidMoveException();
            }
            // TODO: Verify that move is valid
            // TODO: Modify game state
            throw new RuntimeException("Not implemented");
        }
        if (terrainMove) {
            Terrain terrain = board[originY][originX].getTerrain();
            if (!canAct(terrain)) {
                throw new InvalidMoveException();
            }
//            if (terrain.getOwner().getAccountId() != ) { // TODO
//                throw new InvalidMoveException();
//            }
            if (!terrain.isAvailable()) {
                throw new InvalidMoveException();
            }
            if (board[originY][originX].getUnit() != null) {
                throw new InvalidMoveException();
            }
            if (canCreate(terrain.getType(), create)) {
                throw new InvalidMoveException();
            }
            Unit product = new Unit();
            board[originY][originX].setUnit(product);
            product.setType(create);
//            product.setOwner(); // TODO
            product.setHp(100);
            product.setAvailable(false);
        }
        gameRepository.updateGameById(gameId, game);
    }

    private boolean canAct(Terrain terrain) {
        return switch (terrain.getType()) {
            case MEADOW, FOREST, MOUNTAIN, ROAD, SEA, HQ, CITY -> false;
            case BARRACKS, FACTORY, SHIPYARD, AIRBASE -> true;
        };
    }

    private boolean canCreate(Terrain.Type producer, Unit.Type product) {
        UnitCategory productCategory = categorizeUnit(product);
        if (producer == Terrain.Type.BARRACKS && productCategory == UnitCategory.INFANTRY) {
            return true;
        }
        if (producer == Terrain.Type.FACTORY && productCategory == UnitCategory.LAND) {
            return true;
        }
        if (producer == Terrain.Type.SHIPYARD && productCategory == UnitCategory.WATER) {
            return true;
        }
        return producer == Terrain.Type.AIRBASE && productCategory == UnitCategory.AIR;
    }

    private boolean canTravel(Unit.Type unit, Terrain.Type terrain) {
        UnitCategory unitCategory = categorizeUnit(unit);
        TerrainCategory terrainCategory = categorizeTerrain(terrain);
        if (unitCategory == UnitCategory.INFANTRY && terrainCategory == TerrainCategory.LAND) {
            return true;
        }
        if (unitCategory == UnitCategory.LAND && terrainCategory == TerrainCategory.LAND) {
            return true;
        }
        if (unitCategory == UnitCategory.WATER && terrainCategory == TerrainCategory.WATER) {
            return true;
        }
        return unitCategory == UnitCategory.AIR;
    }

    private UnitCategory categorizeUnit(Unit.Type unitType) {
        return switch (unitType) {
            case GRUNT -> UnitCategory.INFANTRY;
            case GROUND_CARRIER, RECON, TANK, AA_MISSILE_LAUNCHER, ROCKET_LAUNCHER -> UnitCategory.LAND;
            case SEA_CARRIER, CRUISER, BATTLESHIP, SUBMARINE -> UnitCategory.WATER;
            case AIR_CARRIER, ATTACK_HELICOPTER, JET, BOMBER -> UnitCategory.AIR;
        };
    }

    private TerrainCategory categorizeTerrain(Terrain.Type terrainType) {
        return switch (terrainType) {
            case MEADOW, FOREST, MOUNTAIN, ROAD, HQ, CITY, BARRACKS, FACTORY, AIRBASE -> TerrainCategory.LAND;
            case SEA, SHIPYARD -> TerrainCategory.WATER;
        };
    }

    private World generateMockWorld(List<Player> players) {
        World world = new World();
        BoardSpot[][] board = new BoardSpot[2][2];
        world.setBoard(board);
        board[0][0] = new BoardSpot();
        board[0][0].setTerrain(new Terrain());
        board[0][0].getTerrain().setType(Terrain.Type.MEADOW);
        board[0][0].setUnit(new Unit());
        board[0][0].getUnit().setType(Unit.Type.GRUNT);
        board[0][0].getUnit().setOwner(players.get(0));
        board[0][0].getUnit().setHp(100);
        board[0][0].getUnit().setAvailable(true);
        board[0][1] = new BoardSpot();
        board[0][1].setTerrain(new Terrain());
        board[0][1].getTerrain().setType(Terrain.Type.HQ);
        board[0][1].getTerrain().setOwner(players.get(0));
        board[0][1].getTerrain().setHp(100);
        board[1][0] = new BoardSpot();
        board[1][0].setTerrain(new Terrain());
        board[1][0].getTerrain().setType(Terrain.Type.HQ);
        board[1][0].getTerrain().setOwner(players.get(1));
        board[1][0].getTerrain().setHp(100);
        board[1][1] = new BoardSpot();
        board[1][1].setTerrain(new Terrain());
        board[1][1].getTerrain().setType(Terrain.Type.BARRACKS);
        board[1][1].getTerrain().setOwner(players.get(1));
        board[1][1].getTerrain().setHp(100);
        board[1][1].getTerrain().setAvailable(true);
        board[1][1].setUnit(new Unit());
        board[1][1].getUnit().setType(Unit.Type.ATTACK_HELICOPTER);
        board[1][1].getUnit().setOwner(players.get(1));
        board[1][1].getUnit().setHp(100);
        board[1][1].getUnit().setAvailable(true);
        return world;
    }

    private enum UnitCategory {
        INFANTRY,
        LAND,
        WATER,
        AIR
    }

    private enum TerrainCategory {
        LAND,
        WATER
    }
}
