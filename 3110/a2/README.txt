# Ranked-Choice Voting Program

This program reads two CSV files — one for **candidates** and one for **ballots** — then validates and tallies the results using a ranked-choice (Borda-style) system.

## How to Run

### Using Dune
dune build
dune exec ./voting_main.exe candidates.csv ballots.csv