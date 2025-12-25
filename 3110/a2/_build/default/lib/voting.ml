(** {1 Ranked-choice Voting Utilities}

    A collection of helper functions for reading, validating, and processing
    ranked-choice ballots. These functions assume CSV-like inputs, represented
    as nested lists of strings.

    - [candidates] : string list list — each inner list represents one candidate
      row, where the first string is the candidate name.
    - [ballots] : string list list — each inner list is a single ballot
      containing candidate names in ranked order. *)

(** {2 Initialization and Scoring Functions} *)

(** [seedData candidates] initializes a tally of all candidates to zero.

    @param candidates A nested list of candidate rows from the CSV file.
    @return A list of (candidate_name, 0) tuples. *)
let seedData candidates =
  List.map
    (fun x ->
      match x with
      | h :: _ -> (h, 0)
      | [] -> ("", 0))
    candidates

(** [incrementData d ele value] increments the score for a specific candidate.

    @param d An association list [(candidate_name, score)].
    @param ele The candidate name to update.
    @param value The amount to add to the candidate’s score.
    @return A new list with the candidate’s score updated. *)
let incrementData d ele value =
  List.map (fun (x, y) -> if x = ele then (x, y + value) else (x, y)) d

(** [setData candidates ballots] computes candidate scores from ranked ballots.

    Each candidate receives points based on their rank in a ballot: the
    first-ranked candidate gets [n - 1] points, the second [n - 2], and so on,
    where [n] is the number of names in that ballot.

    @param candidates A list of candidate rows.
    @param ballots A list of ballots (each ballot is a ranked list of names).
    @return An association list of [(candidate_name, total_score)]. *)
let setData candidates ballots =
  let data = seedData candidates in
  List.fold_left
    (fun acc ballot ->
      let n = List.length ballot in
      let indexed = List.mapi (fun idx cand -> (idx, cand)) ballot in
      List.fold_left
        (fun d (idx, cand) -> incrementData d cand (n - idx - 1))
        acc indexed)
    data ballots

(** {2 Winner Calculation} *)

(** [maxNum data max] returns the maximum score among candidates.

    @param data A list of (candidate_name, score) pairs.
    @param max The current maximum value (start with 0).
    @return The highest score found in the list. *)
let rec maxNum data max =
  match data with
  | [] -> max
  | (_, y) :: t -> if y > max then maxNum t y else maxNum t max

(** [pickWinner data] filters and returns the candidates with the top score.

    @param data A list of (candidate_name, score) pairs.
    @return A sublist containing only those entries with the maximum score. *)
let pickWinner data = List.filter (fun (_, y) -> y = maxNum data 0) data

(** [realWinners data] returns only the names of the winning candidates.

    @param data A list of (candidate_name, score) pairs.
    @return A list of names corresponding to the winners. *)
let realWinners data = List.map fst (pickWinner data)

(** {2 Validation Helpers} *)

(** [has_duplicates lst] checks whether a list contains duplicates.

    @param lst A list of strings (e.g., a single ballot).
    @return [true] if there are duplicates, [false] otherwise. *)
let has_duplicates lst =
  let rec aux seen = function
    | [] -> false
    | x :: xs -> if List.mem x seen then true else aux (x :: seen) xs
  in
  aux [] lst

(** [check_ballot_duplicates ballots] detects ballots with duplicate names.

    @param ballots A list of ballots (each ballot is a list of candidate names).
    @return
      [Some i] if the i-th ballot (1-based index) has duplicates; [None]
      otherwise. *)
let check_ballot_duplicates ballots =
  let rec aux idx = function
    | [] -> None
    | b :: bs -> if has_duplicates b then Some idx else aux (idx + 1) bs
  in
  aux 1 ballots

(** [check_ballot_lengths candidates ballots] ensures each ballot ranks all
    candidates.

    @param candidates The candidate list (nested, as from CSV).
    @param ballots A list of ballots.
    @return [Some i] if ballot [i] has an incorrect length; [None] otherwise. *)
let check_ballot_lengths candidates ballots =
  let expected_len = List.length candidates in
  let rec aux idx = function
    | [] -> None
    | ballot :: rest ->
        if List.length ballot <> expected_len then Some idx
        else aux (idx + 1) rest
  in
  aux 1 ballots

(** [check_invalid_candidates candidates ballots] finds any names not in the
    candidate list.

    @param candidates A nested candidate table ([string list list]).
    @param ballots A list of ballots ([string list list]).
    @return
      [Some (i, bad_name)] if ballot [i] contains an invalid name; [None] if all
      are valid. *)
let check_invalid_candidates (candidates : string list list)
    (ballots : string list list) =
  let all_candidates = List.concat candidates in
  let rec loop idx = function
    | [] -> None
    | ballot :: rest -> (
        let bad_opt =
          List.find_opt (fun name -> not (List.mem name all_candidates)) ballot
        in
        match bad_opt with
        | Some bad -> Some (idx, bad)
        | None -> loop (idx + 1) rest)
  in
  loop 1 ballots
