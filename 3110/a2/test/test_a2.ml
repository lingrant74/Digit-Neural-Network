open OUnit2
open A2.Voting

let candidates1 = Csv.load "../data/candidates.csv"
let ballots1 = Csv.load "../data/ballots.csv"
let data = setData candidates1 ballots1
let winnerList = realWinners data
let candidates2 = Csv.load "../data/customCandidates.csv"
let ballots2 = Csv.load "../data/customBallots.csv"
let data1 = setData candidates2 ballots2
let winnerList1 = realWinners data1

let tests =
  "test suite"
  >::: [
         ( "data Test" >:: fun _ ->
           assert_equal data
             [ ("Vanilla", 23); ("Chocolate", 21); ("Strawberry", 16) ] );
         ("data Test1" >:: fun _ -> assert_equal winnerList [ "Vanilla" ]);
         ( "data Test2" >:: fun _ ->
           assert_equal data1
             [
               ("Python", 114);
               ("JavaScript", 114);
               ("C++", 66);
               ("Rust", 66);
               ("Go", 56);
               ("Haskell", 4);
             ] );
         ( "data Test3" >:: fun _ ->
           assert_equal winnerList1 [ "Python"; "JavaScript" ] );
       ]

let _ = run_test_tt_main tests
