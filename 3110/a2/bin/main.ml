(** {1 Ranked-choice Voting Main Program}

    This executable loads candidate and ballot CSV files, validates their
    integrity, performs ranked-choice tallying, and prints the final scores and
    winner(s).

    Usage:
    {[
      dune exec ./voting_main.exe candidate.csv ballot.csv
    ]} *)

open A2.Voting

(** {2 Input Loading} *)

(** [candidateCVS] — the candidate CSV file provided as the first argument. *)
let candidateCVS = Csv.load Sys.argv.(1)

(** [ballotCVS] — the ballot CSV file provided as the second argument. *)
let ballotCVS = Csv.load Sys.argv.(2)

(** [data] — computed candidate score table based on all ballots. *)
let data = setData candidateCVS ballotCVS

(** [winnerList] — list of winners’ names extracted from the tally. *)
let winnerList = realWinners data

(** {2 File Validation Checks} *)

(** Ensure the first input file has a .csv extension. *)
let () =
  if not (Filename.check_suffix (String.lowercase_ascii Sys.argv.(1)) ".csv")
  then (
    prerr_endline "Error: candidate file does not end in .csv";
    exit 1)
  else ()

(** Ensure the second input file has a .csv extension. *)
let () =
  if not (Filename.check_suffix (String.lowercase_ascii Sys.argv.(2)) ".csv")
  then (
    prerr_endline "Error: ballot file does not end in .csv";
    exit 1)
  else ()

(** Check that the candidate CSV file actually exists. *)
let () =
  if not (Sys.file_exists Sys.argv.(1)) then (
    prerr_endline "Candidate file does not exist!";
    exit 1)
  else ()

(** Check that the ballot CSV file actually exists. *)
let () =
  if not (Sys.file_exists Sys.argv.(2)) then (
    prerr_endline "Ballot file does not exist!";
    exit 1)
  else ()

(** {2 Data Validation Checks} *)

(** Validate that no ballot contains duplicate candidate names. *)
let () =
  if check_ballot_duplicates ballotCVS <> None then (
    prerr_endline
      "Error: a ballot contains duplicate votes for the same candidate!";
    exit 1)
  else ()

(** Validate that every ballot ranks all candidates. *)
let () =
  if check_ballot_lengths candidateCVS ballotCVS <> None then (
    prerr_endline "Error: a ballot did not rank every candidate!";
    exit 1)
  else ()

(** Validate that all ballot names exist in the candidate list. *)
let () =
  if check_invalid_candidates candidateCVS ballotCVS <> None then (
    prerr_endline "Error: ballot contains invalid candidate!";
    exit 1)
  else ()

(** {2 Output Results} *)

(** Print the full tally for all candidates. *)
let () = print_endline "This is the tally plus elements:"

let () =
  List.iter (fun (name, score) -> Printf.printf "%s -> %d\n" name score) data

(** Print the final winner(s). *)
let () = print_endline "The winner is:"

let () = print_endline (String.concat ", " winnerList)
