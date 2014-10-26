class SudokuSquares {
    val digits: String = "123456789"
    val rows: String = "ABCDEFGHI"
    val cols: String = digits

    val squares = cross(rows, cols)
    
    val every_col = cols.map(cross(rows, _))
    val every_row = rows.map(cross(_, cols))
    val every_3x3 = for (str_a <- Vector("ABC", "DEF", "GHI"); str_b <- Vector("123", "456", "789")) yield (cross(str_a, str_b))

    val unitlist = every_col ++ every_row ++ every_3x3

    val units = new collection.mutable.HashMap[String, scala.collection.immutable.IndexedSeq[scala.collection.immutable.IndexedSeq[String]]]
    val peers = new collection.mutable.HashMap[String, scala.collection.immutable.Set[String]]
    for (s <- squares) {
        //units hash contains row, col, and 3x3 vector combinations that contains the key: s
        units(s) = for (vec <- unitlist; if vec.contains(s)) yield (vec)
        //peers hash contains sets of all square related to the key: sq
        peers(s) = units(s).flatten.toSet -- Set(s)
    }

    def test() {
        // "A set of tests that must pass."
        assert (squares.length == 81)
        assert (unitlist.length == 27)
        for (s <- squares) {
            assert (units(s).length == 3)
            assert (peers(s).size == 20)
        }
        assert(units("C2") == Vector(Vector("A2", "B2", "C2", "D2", "E2", "F2", "G2", "H2", "I2"), Vector("C1", "C2", "C3", "C4", "C5", "C6", "C7", "C8", "C9"), Vector("A1", "A2", "A3", "B1", "B2", "B3", "C1", "C2", "C3")))
        assert(peers("C2") == Set("A2", "B2", "D2", "E2", "F2", "G2", "H2", "I2", "C1", "C3", "C4", "C5", "C6", "C7", "C8", "C9", "A1", "A3", "B1", "B3"))
        println("All tests pass.")
    }

    def cross(str_a: String, str_b: String) : scala.collection.immutable.IndexedSeq[String] = {
        // "Cross product of elements in A and elements in B."
        return for (char_a <- str_a; char_b <- str_b) yield (char_a + "" + char_b)
    }

    def cross(str_a: String, char_b: Char) : scala.collection.immutable.IndexedSeq[String] = {
        // "Cross product of elements in A and elements in B."
        return for (char_a <- str_a) yield (char_a + "" + char_b)
    }

    def cross(char_a: Char, str_b: String) : scala.collection.immutable.IndexedSeq[String] = {
        // "Cross product of elements in A and elements in B."
        return for (char_b <- str_b) yield (char_a + "" + char_b)
    }
}



object Sudoku {
    def main(args: Array[String]) {
        val sudoku = new SudokuSquares
        var a: String = "a"
        var b: String = "123"
        var c = sudoku.squares
        println(sudoku.test())
    }
}