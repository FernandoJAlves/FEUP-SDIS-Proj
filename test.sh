#!/bin/bash

# Peers
gnome-terminal -x sh -c 'java Peer 1.0 1 peer_1 224.0.0.1 8081 224.0.0.2 8082 224.0.0.3 8083; exec bash'
gnome-terminal -x sh -c 'java Peer 1.0 2 peer_2 224.0.0.4 8084 224.0.0.5 8085 224.0.0.6 8086; exec bash'
gnome-terminal -x sh -c 'java Peer 1.0 3 peer_3 224.0.0.7 8087 224.0.0.8 8088 224.0.0.9 8089; exec bash'

# Client
gnome-terminal -x sh -c 'java Client localhost peer_1 STATE; exec bash'
gnome-terminal -x sh -c 'java Client localhost peer_2 STATE; exec_bash'
gnome-terminal -x sh -c 'java Client localhost peer_3 STATE; exec bash'