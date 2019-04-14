echo "Options: ";
echo "1 - java TestApp peer_1 BACKUP qwerty.txt 2;";
echo "2 - java TestApp peer_1 DELETE qwerty.txt;";
echo "3 - java TestApp peer_1 RESTORE qwerty.txt;";
echo "4 - java TestApp peer_1 BACKUP penguin.jpg 2;";
echo "5 - java TestApp peer_1 DELETE penguin.jpg;";
echo "6 - java TestApp peer_1 RESTORE penguin.jpg;";
echo "7 - java TestApp peer_2 RECLAIM 0;";
echo "8 - java TestApp peer_3 RECLAIM 0;";

echo "9 - java TestApp peer_1 STATE;";

echo "15 - java TestApp peer_1 BACKUP file64k.txt 2;";
echo "16 - java TestApp peer_1 DELETE file64k.txt;";
echo "17 - java TestApp peer_1 RESTORE file64k.txt;";

echo "21 - java TestApp peer_1 RESTOREENH penguin.jpg;";

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
            # java TestApp 10.227.161.31 peer_1 DELETE penguin.jpg
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
            echo "java TestApp peer_2 RECLAIM 0"
            java TestApp peer_2 RECLAIM 0
        ;;
        8)
            echo "java TestApp peer_3 RECLAIM 0"
            java TestApp peer_3 RECLAIM 0
        ;;
        9)
            echo "java TestApp peer_1 STATE"
            java TestApp peer_1 STATE
        ;;
        15)
            echo "java TestApp peer_1 BACKUP file64k.txt 2"
            java TestApp peer_1 BACKUP file64k.txt 2
        ;;
        16)
            echo "java TestApp peer_1 DELETE file64k.txt"
            java TestApp peer_1 DELETE file64k.txt
        ;;
        17)
            echo "java TestApp peer_1 RESTORE file64k.txt"
            java TestApp peer_1 RESTORE file64k.txt
        ;;
        21)
            echo "java TestApp peer_1 RESTOREENH penguin.jpg"
            java TestApp peer_1 RESTOREENH penguin.jpg
        ;;
        22)
            echo "java TestApp peer_1 RESTOREENH file64k.jpg"
            java TestApp peer_1 RESTOREENH file64k.jpg
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