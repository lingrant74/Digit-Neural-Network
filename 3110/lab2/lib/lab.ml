(* Put your lab code here. *)

let my_name = "grant"

let rec fib n =
  match n with
  | 0 -> 0
  | 1 -> 1
  | _ -> fib (n - 1) + fib (n - 2)

let rec2 (n, m) = (m, m + n)
let rec fib_loop n (x, y) = if n <= 0 then y else fib_loop (n - 1) (rec2 (x, y))

let rec fibi n =
  match n with
  | 0 -> 0
  | _ -> fib_loop (n - 1) (0, 1)
