import scala.math.Ordering.Implicits._

class sudokuSquares {
    val DIGITS: String = "123456789"
    val ROWS: String = "ABCDEFGHI"
    val COLS: String = DIGITS

    val SQUARES = cross(ROWS, COLS)
    
    val every_col = COLS.map(cross(ROWS, _))
    val every_row = ROWS.map(cross(_, COLS))
    val every_3x3 = for (str_a <- Vector("ABC", "DEF", "GHI"); str_b <- Vector("123", "456", "789")) yield (cross(str_a, str_b))

    val UNITLIST = every_col ++ every_row ++ every_3x3

    val UNITS = new collection.mutable.HashMap[String, scala.collection.immutable.IndexedSeq[scala.collection.immutable.IndexedSeq[String]]]
    val PEERS = new collection.mutable.HashMap[String, scala.collection.immutable.Set[String]]
    for (s <- SQUARES) {
        //UNITS hash contains row, col, and 3x3 vector combinations that contains the key: s
        UNITS(s) = for (vec <- UNITLIST; if vec.contains(s)) yield (vec)
        //PEERS hash contains sets of all square related to the key: sq
        PEERS(s) = UNITS(s).flatten.toSet -- Set(s)
    }

    def test() {
        // "A set of tests that must pass."
        assert (SQUARES.length == 81)
        assert (UNITLIST.length == 27)
        for (s <- SQUARES) {
            assert (UNITS(s).length == 3)
            assert (PEERS(s).size == 20)
        }
        assert(UNITS("C2") == Vector(Vector("A2", "B2", "C2", "D2", "E2", "F2", "G2", "H2", "I2"), Vector("C1", "C2", "C3", "C4", "C5", "C6", "C7", "C8", "C9"), Vector("A1", "A2", "A3", "B1", "B2", "B3", "C1", "C2", "C3")))
        assert(PEERS("C2") == Set("A2", "B2", "D2", "E2", "F2", "G2", "H2", "I2", "C1", "C3", "C4", "C5", "C6", "C7", "C8", "C9", "A1", "A3", "B1", "B3"))
        println("All tests pass.")
    }

    // "Cross product of elements in A and elements in B."
    def cross(str_a: String, str_b: String) : scala.collection.immutable.IndexedSeq[String] = {
        return for (char_a <- str_a; char_b <- str_b) yield (char_a + "" + char_b)
    }
    def cross(str_a: String, char_b: Char) : scala.collection.immutable.IndexedSeq[String] = {
        return for (char_a <- str_a) yield (char_a + "" + char_b)
    }
    def cross(char_a: Char, str_b: String) : scala.collection.immutable.IndexedSeq[String] = {
        return for (char_b <- str_b) yield (char_a + "" + char_b)
    }
}


class SudokuBoard {
    val sudokuSquares = new sudokuSquares
    var values = new collection.mutable.HashMap[String, String]

    // ################ Parse a Grid ################


    def calc_init_possible_moves() : collection.mutable.HashMap[String, String] = {
        //adds all possible values the square can take on by removing value of square's PEERS from all DIGITS
        val processed_values = collection.mutable.HashMap[String, String]() ++= values
        for (s <- sudokuSquares.SQUARES; if "0.".contains(values(s))) {
            processed_values(s) = sudokuSquares.DIGITS
            for (s2 <- sudokuSquares.PEERS(s)) processed_values(s) = processed_values(s).replace(values(s2), "")
        }
        processed_values("state") = "passed"
        return processed_values
    }

    def init_grid_value(grid: String) {
        // "Convert grid into a dict of {square: char} with '0' or '.' for empties."
        val chars = for (c <- grid; if sudokuSquares.DIGITS.contains(c) || "0.".contains(c)) yield (c + "")
        assert(chars.length == 81)
        for (el <- sudokuSquares.SQUARES.zip(chars)) values(el._1) = el._2
    }

    // ################ Constraint Propagation ################


    def assign(vals: collection.mutable.HashMap[String, String], s:String, d:String) : collection.mutable.HashMap[String, String] = {
        // """Eliminate all the other values (except d) from values[s] and propagate.
        val other_values = vals(s).replace(d, "")
        if (other_values.forall(d2 => eliminate(vals, s, d2 + "")("state") == "passed")) {
            vals("state") = "passed"
            return vals
        } else {
            vals("state") = "failed"
            return vals
        }
    }

    def eliminate(vals: collection.mutable.HashMap[String, String], s:String, d:String) : collection.mutable.HashMap[String, String] = {
        // """Eliminate d from values[s]; propagate when values or places <= 2.
        // Return values, except return False if a contradiction is detected."""
        vals("state") = "failed"
        if (!vals(s).contains(d)) {
            vals("state") = "passed"
            return vals  // Already eliminated
        }
        vals(s) = vals(s).replace(d, "")
        // ## (1) If a square s is reduced to one value d2, then eliminate d2 from the PEERS.
        if (vals(s).length == 0) {
            return vals
        } else if (vals(s).length == 1) {
            val d2 = vals(s)
            if (!sudokuSquares.PEERS(s).forall(s2 => eliminate(vals, s2, d2)("state") == "passed")) {
                return vals
            }
        }
        // ## (2) If a unit u is reduced to only one place for a value d, then put it there.
        for (u <- sudokuSquares.UNITS(s)) {
            val dplaces = for (s <- u; if vals(s).contains(d)) yield (s)
            if (dplaces.length == 0) {
                return vals
            } else if (dplaces.length == 1) {
                if (!(assign(vals, dplaces(0), d)("state") == "passed"))
                    return vals
            }
        }
        vals("state") = "passed"
        return vals
    }

    // ################ Display as 2-D grid ################


    def display(vals: collection.mutable.HashMap[String, String] = values) {
        // "Display these values as a 2-D grid."
        val width = 1 + (for (s <- sudokuSquares.SQUARES) yield (vals(s).length)).max
        val line_el = List.fill(width * 3)("-").mkString
        val line = List.fill(3)(line_el).mkString("+")

        for (r <- sudokuSquares.ROWS) {
            println((for (c <- sudokuSquares.COLS) yield (vals(r + "" + c) + " " + (if ("36".contains(c)) "|" else ""))).mkString)
            if ("CF".contains(r))
                println(line)
        }
    }

    // ################ Search ################


    def solve(vals: collection.mutable.HashMap[String, String]) : collection.mutable.HashMap[String, String] = {
        return search(vals)
    }

    def search(vals: collection.mutable.HashMap[String, String]) : collection.mutable.HashMap[String, String] = {
        // "Using depth-first search and propagation, try all possible values."
        if (vals("state") == "failed") { return vals }
        if (vals("state") == "passed" && sudokuSquares.SQUARES.forall(s => vals(s).length == 1)) {
            vals("state") = "solved"
            return vals
        }

        //sort, return the square with the fewest number of values, and use that as the next move
        def diff(t2: (Int, String)) = -t2._1
        val pq = new collection.mutable.PriorityQueue[(Int, String)]()(Ordering.by(diff))
        for (s <- sudokuSquares.SQUARES; if vals(s).length > 1) pq.enqueue(vals(s).length -> s)
        val min_s = pq.dequeue()

        // val vals_copy = collection.mutable.HashMap[String, String]() ++= vals
        return some( for (d <- vals(min_s._2)) yield search(assign(new collection.mutable.HashMap[String, String]() ++= vals, min_s._2, d + "")) )

        return vals
    }

    def some(arr: scala.collection.immutable.IndexedSeq[collection.mutable.HashMap[String, String]]) : collection.mutable.HashMap[String, String] = {
        for (vals <- arr; if (vals("state") == "solved")) return vals
        values("state") = "failed"
        return values
    }

    // ################ System test ################


    def solve_all(grid: String) {
        val start = System.nanoTime
        //time the performance of algorithm
        init_grid_value(grid)
        val solution = solve(calc_init_possible_moves())
        println(solution("state"))

        println("(" + ((System.nanoTime - start) / 1e9).toString() + "sec)")
        if (solution("state") == "solved") values = solution
    }

    // ################ FILE I/O ###################


    def read_csv(filename: String) : String = {
        val src = scala.io.Source.fromFile("input.csv").getLines()
        var input: String = ""
        for (line <- src) { input += line.split(",").mkString("") }
        return input
    }

    def write_csv(filename: String) {
        val pw = new java.io.PrintWriter(new java.io.File(filename))
        for (r <- sudokuSquares.ROWS) { pw.write((for (c <- sudokuSquares.COLS) yield (values(r + "" + c) + ",")).mkString + System.lineSeparator) }
        pw.close()
    }
}


object Sudoku {
    def main(args: Array[String]) {
        val sudokuBoard = new SudokuBoard
        var input: String = "input.csv"
        var output: String = "output.csv"
        var grid = sudokuBoard.read_csv(input)
    
        sudokuBoard.solve_all(grid)
        sudokuBoard.display()
        sudokuBoard.write_csv(output)
    }
}