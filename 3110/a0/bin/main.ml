let () =
  print_endline
    "Pick a Category\n\
     1. Artist of the Year\n\
     2. Best New Artist\n\
     Type either 1 or 2"

let x = read_line ();;

if x = "1" then
  print_endline
    "Winner: \n\
     Lady Gaga\n\n\
     Nominees:\n\
     Bad Bunny\n\
     Beyoncé\n\
     Kendrick Lamar\n\
     Taylor Swift\n\
     Morgan Wallen\n\
     The Weeknd\n"
else
  print_endline
    "Winner: \n\
     Alex Warren\n\n\
     Nominees:\n\
     Ella Langley\n\
     Gigi Perez\n\
     Lola Young\n\
     Sombr\n\
     The Marías\n"
