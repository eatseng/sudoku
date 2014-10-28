Sudoku - Insight Data Engineering Challenge
===========================================

In this coding exercise, I have studied A Pencil-and-Paper Algorithm for Solving Sudoku Puzzles(http://www.ams.org/notices/200904/tx090400460p.pdf) and Solving Every Sudoku Puzzle(http://norvig.com/sudoku.html), and I made the following revamp to Peter Norvig's code:

1. Converted Python into Scala (first time coding in Scala)
2. Created an efficient algorithm to populate initial squares
3. Caching failing DFS attempts
 
The the improved algorithm result:
1. ~200% more efficient on easy puzzles
2. > 700% on hard puzzles.

I also have other collections of games I have programmed in the past on github (https://github.com/eatseng/appacademy/tree/master/games) that can be candidates of evaluation. [Project Asteroid can be played here http://morning-ocean-9992.herokuapp.com/ Controls are w-s-a-d, f to fire] Thank you.

To Run
------
Specifiy the csv input filename in var input and output csv filename in var output.
To compile code - "scalac -d sudoku_class sudoku.scala"
To execute code - "scala -cp sudoku_class Sudoku"

Sudoku Strategy
---------------
The idea of Sudoku is to compare all row, col, and 3x3 squares that the square in question occupies, and make sure there is no duplicate value amongst its peers of (9 + 9 + 9 - 4 [3x3 squares double counts 2 row and 2 col squares] - 3[double count of square in question]) = 20 squares.

Peter Norvig's algorithm is to populate every square with a string of all possible values 1~9, then assign value to each square from the input file using (1).

(1) Upon assignment of value to a square, the algorithm eliminates all other possible values from that square, and eliminate the assigned value from all other peer squares. (2) During that elimination, it also checks other squares to see how many possible values each other square can take on. If other squares can only take on one possible value, then that value is assigned to those squares - this lead to (1) for those squares - and the recursion continues until the values of all squares have been adjusted.

His algorithm then select a square and assign it with value that has the highest likelihood of being the right value (square with the fewest number of possible values,) using (1). It continues to pick square and fill-in values until all permutations have been tried, and then it selects a random solution from a set of solutions (if there are any, else returns false.)

The bottleneck of the algorithm is not so much with recursion, but with copy of dict/hash that stores values of all squares (Dict/Hash initialization and copy are very very expensive.) that is needed for depth first search (DFS.) My focus of this project has been to learn scala as well as to reduce the number of times DFS is executed (which resulted in nearly 10X performance gain.) 
