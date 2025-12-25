let is_prime n =
  if n < 2 then false
  else if n = 2 then true
  else if n mod 2 = 0 then false
  else
    let limit = int_of_float (sqrt (float_of_int n)) in
    let rec loop d =
      if d > limit then true
      else if n mod d = 0 then false
      else loop (d + 2)
    in
    loop 3

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
