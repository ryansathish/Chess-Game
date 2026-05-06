# Console Chess Game — Phase 1

A two-player console-based chess game implemented in Java.

---

## Project Structure

```
chess/
├── src/
│   ├── Main.java                  # Entry point
│   ├── pieces/
│   │   ├── Piece.java             # Abstract base class
│   │   ├── Pawn.java
│   │   ├── Rook.java
│   │   ├── Knight.java
│   │   ├── Bishop.java
│   │   ├── Queen.java
│   │   └── King.java
│   ├── board/
│   │   └── Board.java             # 8x8 grid, move execution, check/checkmate
│   ├── player/
│   │   └── Player.java            # Input handling, move parsing
│   ├── game/
│   │   └── Game.java              # Main game loop, turn management
│   └── utils/
│       ├── Position.java          # Row/col coordinate model
│       └── Utils.java             # Parsing and validation helpers
├── build.sh                       # Compile script
├── generate-docs.sh               # Javadoc generation script
└── README.md
```

---

## How to Build & Run

### Prerequisites
- Java JDK 11 or higher

### Compile
```bash
chmod +x build.sh
./build.sh
```

Or manually:
```bash
mkdir -p out
javac -d out -sourcepath src $(find src -name "*.java")
```

### Run
```bash
java -cp out Main
```

### Generate Javadoc
```bash
chmod +x generate-docs.sh
./generate-docs.sh
# Open docs/index.html in your browser
```

---

## How to Play

The board is displayed after every move with file labels (A–H) and rank numbers (1–8).

### Move Format

| Action           | Input Example  | Notes                              |
|------------------|----------------|------------------------------------|
| Move a piece     | `E2 E4`        | FROM square then TO square         |
| Capture          | `D5 E6`        | Same format — capture is automatic |
| Kingside castle  | `O-O`          | King and h-rook must not have moved|
| Queenside castle | `O-O-O`        | King and a-rook must not have moved|
| Pawn promotion   | `E7 E8=Q`      | Q=Queen, R=Rook, B=Bishop, N=Knight|
| Quit game        | `quit`         |                                    |

### Piece Symbols

| Piece  | White | Black |
|--------|-------|-------|
| Pawn   | `wp`  | `bp`  |
| Rook   | `wR`  | `bR`  |
| Knight | `wN`  | `bN`  |
| Bishop | `wB`  | `bB`  |
| Queen  | `wQ`  | `bQ`  |
| King   | `wK`  | `bK`  |

---

## Phase 1 Checklist

- [x] Package structure (`pieces`, `board`, `player`, `game`, `utils`)
- [x] Abstract `Piece` class with `Color` enum
- [x] All 6 piece subclasses with correct movement logic
- [x] `Board` class — 8x8 grid, initialization, `display()`, `movePiece()`, `isCheck()`, `isCheckmate()`, `isStalemate()`
- [x] `Player` class — console input, move parsing
- [x] `Game` class — game loop, turn alternation, check/checkmate detection, castling, promotion
- [x] `Position` and `Utils` helper classes
- [x] Full Javadoc on all classes, methods, and attributes
- [x] `build.sh` and `generate-docs.sh` scripts
- [x] Move format validation (`E2 E4`, `O-O`, `O-O-O`, `E7 E8=Q`)

---

## Design Notes

- **`Piece.possibleMoves(Piece[][] board)`** returns candidate squares based on movement rules only. The `Board` filters these for legality (check exposure).
- **`Board.isLegalMove()`** simulates each candidate move on a copied grid to verify the moving player's king is not left in check.
- **Castling** is handled in `Game.executeCastle()` — validates empty squares, no check passing.
- **Promotion** defaults to Queen if no promotion piece is specified.
