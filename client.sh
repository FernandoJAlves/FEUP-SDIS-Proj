echo "Options: ";
echo "1 - java Client localhost peer_1 BACKUP qwerty.txt 1;";
echo "2 - java Client localhost peer_1 DELETE penguin.jpg;";

echo "0 - Close Options";
echo Value: ;
read val;
run_j_cmd () {
    case $1 in
        1)
            echo "java Client localhost peer_1 BACKUP qwerty.txt 1"
            java Client localhost peer_1 BACKUP qwerty.txt 1
            # java Client 10.227.161.31 peer_1 BACKUP penguin.jpg 1
        ;;
        2)
            echo "java Client localhost peer_1 DELETE penguin.jpg"
            java Client localhost peer_1 DELETE penguin.jpg
            # java Client 10.227.161.0 peer_1 DELETE penguin.jpg
        ;;
        0)
            echo "Leaving..."
            exit 1
        ;;
    esac
}

run_j_cmd $val;
sleep 1s;
sh client.sh;