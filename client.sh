echo "Options: ";
echo "0 - java Client localhost peer_3 BACKUP penguin.jpg 1;";
echo "1 - java Client localhost peer_3 DELETE penguin.jpg;";
echo Value: ;
read val;
sleep 1s;
run_j_cmd () {
    case $1 in
        0)
            java Client localhost peer_3 BACKUP penguin.jpg 1
        ;;
        1)
            java Client localhost peer_3 DELETE penguin.jpg
        ;;
    esac
}

run_j_cmd $val;