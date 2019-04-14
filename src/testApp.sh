echo "Options: ";
echo "1 - java TestApp peer_1 BACKUP qwerty.txt 2;";
echo "2 - java TestApp peer_1 DELETE qwerty.txt;";
echo "3 - java TestApp peer_1 RESTORE qwerty.txt;";

echo "\n4 - java TestApp peer_1 BACKUP penguin.jpg 2;";
echo "5 - java TestApp peer_1 DELETE penguin.jpg;";
echo "6 - java TestApp peer_1 RESTORE penguin.jpg;";

echo "\n7 - java TestApp peer_1 BACKUP file64k.txt 2;";
echo "8 - java TestApp peer_1 DELETE file64k.txt;";
echo "9 - java TestApp peer_1 RESTORE file64k.txt;";

echo "\n21 - java TestApp peer_1 RESTOREENH qwerty.txt;";
echo "22 - java TestApp peer_1 RESTOREENH file64k.txt";
echo "23 - java TestApp peer_1 RESTOREENH penguin.jpg;";
echo "24 - java TestApp peer_1 DELETEENH qwerty.txt;";
echo "25 - java TestApp peer_1 DELETEENH file64k.txt;";
echo "26 - java TestApp peer_1 DELETEENH penguin.jpg;";

echo "31 - java TestApp peer_1 RECLAIM 0;";
echo "32 - java TestApp peer_2 RECLAIM 0;";
echo "33 - java TestApp peer_3 RECLAIM 0;";

echo "41 - java TestApp peer_1 STATE;";
echo "42 - java TestApp peer_2 STATE;";
echo "43 - java TestApp peer_3 STATE;";

echo "0 - Close Options";
echo Value: ;
read val;
run_j_cmd () {
    case $1 in
        1)
            echo "java TestApp peer_1 BACKUP qwerty.txt 2"
            java TestApp peer_1 BACKUP qwerty.txt 2
        ;;
        2)
            echo "java TestApp peer_1 DELETE qwerty.txt"
            java TestApp peer_1 DELETE qwerty.txt
        ;;
        3)
            echo "java TestApp peer_1 RESTORE qwerty.txt"
            java TestApp peer_1 RESTORE qwerty.txt
        ;;
        4)
            echo "java TestApp peer_1 BACKUP penguin.jpg 2"
            java TestApp peer_1 BACKUP penguin.jpg 2
        ;;
        5)
            echo "java TestApp peer_1 DELETE penguin.jpg"
            java TestApp peer_1 DELETE penguin.jpg
        ;;
        6)
            echo "java TestApp peer_1 RESTORE penguin.jpg"
            java TestApp peer_1 RESTORE penguin.jpg
        ;;
        7)
            echo "java TestApp peer_1 BACKUP file64k.txt 2"
            java TestApp peer_1 BACKUP file64k.txt 2
        ;;
        8)
            echo "java TestApp peer_1 DELETE file64k.txt"
            java TestApp peer_1 DELETE file64k.txt
        ;;
        9)
            echo "java TestApp peer_1 RESTORE file64k.txt"
            java TestApp peer_1 RESTORE file64k.txt
        ;;
        21)
            echo "java TestApp peer_1 RESTOREENH qwerty.txt"
            java TestApp peer_1 RESTOREENH qwerty.txt
        ;;
        22)
            echo "java TestApp peer_1 RESTOREENH file64k.txt"
            java TestApp peer_1 RESTOREENH file64k.txt
        ;;
        23)
            echo "java TestApp peer_1 RESTOREENH penguin.jpg"
            java TestApp peer_1 RESTOREENH penguin.jpg
        ;;
        24)
            echo "java TestApp peer_1 DELETEENH qwerty.txt"
            java TestApp peer_1 DELETEENH qwerty.txt
        ;;
        25)
            echo "java TestApp peer_1 DELETEENH file64k.txt"
            java TestApp peer_1 DELETEENH file64k.txt
        ;;
        26)
            echo "java TestApp peer_1 DELETEENH penguin.jpg"
            java TestApp peer_1 DELETEENH penguin.jpg
        ;;
        31)
            echo "java TestApp peer_1 RECLAIM 0"
            java TestApp peer_1 RECLAIM 0
        ;;
        32)
            echo "java TestApp peer_2 RECLAIM 0"
            java TestApp peer_2 RECLAIM 0
        ;;
        33)
            echo "java TestApp peer_3 RECLAIM 0"
            java TestApp peer_3 RECLAIM 0
        ;;
        41)
            echo "java TestApp peer_1 STATE"
            java TestApp peer_1 STATE
        ;;
        42)
            echo "java TestApp peer_2 STATE"
            java TestApp peer_2 STATE
        ;;
        43)
            echo "java TestApp peer_3 STATE"
            java TestApp peer_3 STATE
        ;;
        0)
            echo "Leaving..."
            exit 1
        ;;
    esac
}

run_j_cmd $val;
sleep 1s;
sh testApp.sh;