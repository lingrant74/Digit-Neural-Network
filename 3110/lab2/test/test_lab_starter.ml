open OUnit2
open Lab_starter.Lab

let test_cases : OUnit2.test list =
  [ ("trivial test" >:: fun _ -> assert_equal 0 0) ]

let test_suite = "lab" >::: test_cases
let () = run_test_tt_main test_suite
