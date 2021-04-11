# Sudoku

Cilj projekta je izrada sustava za rješavanje i generiranje sudoku zagonetki raznih vrsta. Osnovna pravila sudoku zagonetki su da svaki redak i stupac kvadratne mreže dimenzija x * x sadrže svaki od brojeva od 1 do x točno jednom. Dio mreže je zadan, a posao rješavača je da ju nadopuni prema navedenim pravilima. Sudoku zagonetke su u ovom dijelu svojih pravila iste kao i latinski kvadrat (negdje se naziva i čarobni kvadrat) ali uvode i dodatna ograničenja u odnosu na njega. Uz retke i stupce, postoje i kutije ili regije, odnosno iscrtani razgraničeni oblici unutar mreže (obično su označeni podebljanim rubom ćelija ili različitom bojom pozadine ćelija) koji također sadrže x ćelija i u kojima se svaki od brojeva od 1 do x mora pojavljivati točno jednom. Konvencija je da te kutije budu manji kvadrati koji se ne preklapaju i čija visina i širina odgovaraju korijenu visine i širine cjelokupne mreže. Zbog ovog ograničenja, sudoku zagonetke su najčešće dimenzija 9 * 9 s kutijama dimenzija 3 * 3, ali s vremenom su kreativniji engimatičari stvorili pregršt zagonetki s pravokutnim kutijama (poput sudoku zagonetke dimenzija 6 * 6 s kutijama dimenzija 2 * 3), kutijama nepravilnih rubova ili sa više slojeva kutija koje se preklapaju. U nekim varijantama zagonetki postavljaju se i dodatna ograničenja poput onog da vrijednost u jednoj ćeliji mora biti manja od vrijednosti u susjednoj ćeliji, ili pak veća od vrijednosti u susjednoj ćeliji, a u nekim sudoku zagonetkama uvodi se pravilo da se brojevi ne smiju ponavljati unutar niti jedne od dijagonala cjelokupne mreže. 

Prilikom zadavanja sudoku zagonetki, moramo biti posebno pažljivi da ne postavimo zagonetku koja ima više od jednog točnog rješenja prema prethodno postavljenim pravilima. Ovo se lako može dogoditi kada u mreži postoji premalen broj unaprijed zadanih ćelija. Posao provjere mogućih rješenja uvelike nam olakšavaju računalni alati, jer je svaka sudoku zagonetka kombinatorni problem. Otkriveno je da je najmanji mogući broj zadanih ćelija 17. Možemo si priuštiti i isprobavanje rješenja metodom grube sile (brute force) u nekim slučajevima, na primjer kada treba samo jednom pronaći rješenje zagonetke za koju znamo da ima jednoznačno rješenje. 