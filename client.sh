echo "Options: ";
echo "1 - java Client localhost peer_3 BACKUP penguin.jpg 1;";
echo "2 - java Client localhost peer_3 DELETE penguin.jpg;";

echo "0 - Close Options";
echo Value: ;
read val;
run_j_cmd () {
    case $1 in
        1)
            echo "java Client localhost peer_3 BACKUP penguin.jpg 1"
            java Client localhost peer_3 BACKUP penguin.jpg 1
        ;;
        2)
            echo "java Client localhost peer_3 DELETE penguin.jpg"
            java Client localhost peer_3 DELETE penguin.jpg
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