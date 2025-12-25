(* prime.ml — efficient integer-only primality test using 6k ± 1 *)

let is_prime n =
  (* Handle small / edge cases quickly *)
  if n <= 1 then false
  else if n <= 3 then true         (* 2 and 3 are prime *)
  else if n mod 2 = 0 || n mod 3 = 0 then false
  else
    (* Check only divisors of the form 6k ± 1 up to sqrt(n).
       Use i > n / i to avoid floats and overflow in i * i. *)
    let rec loop i =
      if i > n / i then true
      else if n mod i = 0 || n mod (i + 2) = 0 then false
      else loop (i + 6)
    in
    loop 5

let () =
  if Array.length Sys.argv <> 2 then (
    prerr_endline "Usage: prime <integer>";
    exit 1
  );
  let n =
    try int_of_string Sys.argv.(1)
    with Failure _ ->
      prerr_endline "Error: argument must be an integer";
      exit 1
  in
  print_endline (if is_prime n then "prime" else "not prime")