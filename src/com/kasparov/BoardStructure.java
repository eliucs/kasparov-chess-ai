package com.kasparov;

import com.kasparov.unsigned.UInt64;

/**
 * Structure and properties of the chess board.
 *
 * @author Eric Liu
 */
public class BoardStructure {

    /**
     * Represents the board pieces on the entire size 120 board.
     */
    int[] pieces = new int[BoardConstants.BOARD_SQR_NUM];

    /**
     * 64 bits represents 8 x 8 board, where 1 denotes pawn at that
     * location, and 0 denotes no pawn at that location.
     */
    long[] pawns = new long[3];

    /**
     * Locations of the two kings.
     */
    int[] kingSqr = new int[2];

    /**
     * Current side to move.
     */
    int side;

    /**
     * Current active en passant square.
     */
    int enPassant;

    /**
     * Counter for the 50-move rule.
     */
    int fiftyMove;

    /**
     * Number of half moves in current search.
     */
    int ply;

    /**
     * Total number of half moves in entire game.
     */
    int historyPly;

    /**
     * Unique 64 bit key for the current position.
     */
    long positionKey;

    /**
     * Number of pieces, by piece type.
     */
    int[] pieceNum = new int[13];

    /**
     * Number of big pieces (any piece not a pawn), by color
     * (white, black, both).
     */
    int[] pieceBig = new int[3];

    /**
     * Number of major pieces (queens and rooks), by color
     * (white, black, both).
     */
    int[] pieceMajor = new int[3];

    /**
     * Number of minor pieces (bishops and knights), by color
     * (white, black, both).
     */
    int[] pieceMinor = new int[3];

    /**
     * Castle permissions.
     */
    int castlePerm;

    /**
     * Keeps track of the history of the game.
     */
    UndoStructure[] history = new UndoStructure[BoardConstants.MAX_GAME_MOVES];

    /**
     * Maps square index from size 120 board to size 64 board.
     */
    int[] sqr120ToSqr64 = new int[BoardConstants.BOARD_SQR_NUM];

    /**
     * Maps square index from size 64 board to size 120 board.
     */
    int[] sqr64ToSqr120 = new int[64];

    /**
     * Piece list.
     */
    int[][] pieceList = new int[13][10];

    /**
     * Bit table.
     */
    int[] bitTable = {
        63, 30,  3, 32, 25, 41, 22, 33,
        15, 50, 42, 13, 11, 53, 19, 34,
        61, 29,  2, 51, 21, 43, 45, 10,
        18, 47,  1, 54,  9, 57,  0, 35,
        62, 31, 40,  4, 49,  5, 52, 26,
        60,  6, 23, 44, 46, 27, 56, 16,
         7, 39, 48, 24, 59, 14, 12, 55,
        38, 28, 58, 20, 37, 17, 36,  8
    };

    /**
     * Initializes an empty BoardStructure.
     */
    public BoardStructure() {}

    /**
     * Initializes the array mappings from the size 120 board to
     * the size 64 board.
     */
    public void initSqr120AndSqr64() {
        int sqr;
        int sqr64 = 0;

        for (int i = 0; i < BoardConstants.BOARD_SQR_NUM; i++)
            this.sqr120ToSqr64[i] = 65;

        for (int r = BoardRank.RANK_1.value; r <= BoardRank.RANK_8.value; r++) {
            for (int f = BoardFile.FILE_A.value; f <= BoardFile.FILE_H.value; f++) {
                sqr = BoardConstants.convertFileRankToSqr(f, r);
                sqr64ToSqr120[sqr64] = sqr;
                sqr120ToSqr64[sqr] = sqr64;
                sqr64++;
            }
        }
    }

    /**
     * Prints the size 120 board.
     *
     * @param hideOuter
     */
    public void printSqr120(boolean hideOuter) {
        for (int i = 0; i < BoardConstants.BOARD_SQR_NUM; i++) {
            if (i % 10 == 0)
                System.out.println();
            if (this.sqr120ToSqr64[i] < 10)
                System.out.print(" ");
            if (hideOuter && this.sqr120ToSqr64[i] == 65)
                System.out.print(" * ");
            else
                System.out.print(this.sqr120ToSqr64[i] + " ");
        }
        System.out.println();
    }

    /**
     * Prints the size 64 board.
     */
    public void printSqr64() {
        for (int i = 0; i < 64; i++) {
            if (i % 8 == 0)
                System.out.println();
            System.out.print(this.sqr64ToSqr120[i] + " ");
        }
        System.out.println();
    }

    /**
     * Prints the bitboard.
     *
     * @param bitboard
     */
    public void printBitBoard(long bitboard) {

        int sqr;
        int sqr64;

        System.out.println();
        for (int r = BoardRank.RANK_8.value; r >= BoardRank.RANK_1.value; r--) {
            for (int f = BoardFile.FILE_A.value; f <= BoardFile.FILE_H.value; f++) {
                sqr = BoardConstants.convertFileRankToSqr(f, r);
                sqr64 = this.sqr120ToSqr64[sqr];

                if (((1L << sqr64) & bitboard) != 0)
                    System.out.print("X ");
                else
                    System.out.print("- ");
            }
            System.out.println();
        }
        System.out.println();
    }

    /**
     * Pops a bit from the bitboard.
     *
     * @param bitboard
     */
    public long[] popBit(long bitboard) {
        long b = bitboard ^ (bitboard - 1);
        UInt64 fold = new UInt64((b & 0xffffff) ^ (b >> 32));
        bitboard &= (bitboard - 1);
        long[] result = new long[2];
        result[0] = bitboard;
        int index = (int) ((fold.longValue() * 0x783a9b23) >> 26);

        System.out.println(index);

        result[1] = bitTable[index];
        return result;
    }

    /**
     * Counts the number of 1 bits in the bitboard.
     *
     * @param bitboard
     */
    public int countBits(long bitboard) {
        int r;
        for (r = 0; bitboard != 0; r++, bitboard &= bitboard - 1);
        return r;
    }

    /**
     * Get index from size 120 board to size 64 board.
     *
     * @param i
     */
    public int sqr64(int i) {
        return this.sqr120ToSqr64[i];
    }

}
