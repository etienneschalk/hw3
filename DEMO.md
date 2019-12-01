# network programming - hw3 - file catalog

## Firefox tabs

- [localhost/phpmyadmin](http://localhost/phpmyadmin/db_structure.php?server=1&db=test)
- [user](http://localhost/phpmyadmin/sql.php?db=test&table=user&pos=0)
- [file](http://localhost/phpmyadmin/sql.php?db=test&table=file&pos=0)

## Folders

- Open C:\Users\etis3\Desktop\hm3client (simulating a client folder, we can choose another but we use it)
- Open C:\Users\etis3\Desktop\hm3server (the actual folder of the server)
- Delete angela.jpeg and angelaTRUE.jpeg if they exist in the server 

## Commands
register 	username 		password
login 		username 		password
list 
details 	serverFileName
upr 		pathToFile 		newName
upw 		pathToFile 		newName
down 		serverFileName 	pathToWantedFolder 	downloadedFileName
delete 		serverFileName
logout (similar to quit)
quit
help

## Demo

[1] = angela merkel 
[2] = emmanuel macron

-------------

[1]
login angela merkel

[1]
help

list

#### if existing
delete angela.jpeg

upw C:/Users/etis3/Desktop/hm3client/angela.jpeg angela.jpeg

list

[2]
login emmanuel macron

details angela.jpeg

delete angela.jpeg

upr C:/Users/etis3/Desktop/hm3client/manu.jpeg angela.jpeg

[1]

#### with trailing slash
down angela.jpeg C:/Users/etis3/Desktop/hm3client/ downangela.jpeg

#### constat this is not the good picture
#### up read only
upr C:/Users/etis3/Desktop/hm3client/angela.jpeg angela.jpeg

#### cannot delete
delete angela.jpeg

upr C:/Users/etis3/Desktop/hm3client/angela.jpeg angelaTRUE.jpeg

down angela.jpeg C:/Users/etis3/Desktop/hm3client/ downangelaTRUE.jpeg

